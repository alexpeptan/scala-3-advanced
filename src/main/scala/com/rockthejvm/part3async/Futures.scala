package com.rockthejvm.part3async

import java.util.concurrent.{ExecutorService, Executors}
import scala.concurrent.{Await, ExecutionContext, Future, Promise}
import scala.util.{ Failure, Random, Success, Try }
import scala.concurrent.duration.*


object Futures {

  def calculateMeaningOfLife(): Int = {
    // simulate long compute
    Thread.sleep(1000)
    42
  }

  // thread pool (Java-specific)
  val executor: ExecutorService = Executors.newFixedThreadPool(4)
  // thread pool (Scala-specific)
  given executionContext: ExecutionContext = ExecutionContext.fromExecutorService(executor)

  // a future = an async computation that will finish at some point
  val aFuture: Future[Int] = Future.apply(calculateMeaningOfLife()) // inspect the value of the future RIGHT NOW

  // Option[Try[Int]], because:
  // - we don't know if we have a value
  // - if we do, that can be a failed computation
  val futureInstantResult: Option[Try[Int]] = aFuture.value

  // callbacks
  aFuture.onComplete {
    case Success(value) => println(s"I've completed the meaning of life: $value")
    case Failure(ex) => println(s"My async computation failed: $ex")
  } // on SOME other thread - 0 control on when or on which thread will be evaluated.

  /*
    Functional composition
   */
  case class Profile(id: String, name: String) {
    def sendMessage(anotherProfile: Profile, message: String) =
      println(s"${this.name} sending message to ${anotherProfile.name}: $message")
  }

  object SocialNetwork {
    // "database"
    val names = Map(
      "rtjvm.id.1-daniel" -> "Daniel",
      "rtjvm.id.1-jane" -> "Jane",
      "rtjvm.id.1-mark" -> "Mark",
    )

    // friends "database"
    val friends = Map(
      "rtjvm.id.1-jane" -> "rtjvm.id.1-mark"
    )

    val random = new Random()
    // "API"
    def fetchProfile(id: String): Future[Profile] = Future {
      // fetch something from the database
      Thread.sleep(random.nextInt(300)) // simulate the time delay
      Profile(id, names(id))
    }

    def fetchBestFriend(profile: Profile): Future[Profile] = Future {
      Thread.sleep(random.nextInt(400))
      val bestFriendId = friends(profile.id)
      Profile(bestFriendId, names(bestFriendId))
    }
  }

  // problem: sending a message to my best friend
  def sendMessageToBestFriend(accountId: String, message: String): Unit = {
    // 1 - call fetchProfile
    // 2 - call fetchBestFriend
    // 3 - call profile.sendMessage(bestFriend)
    val profileFuture = SocialNetwork.fetchProfile(accountId)

    profileFuture.onComplete {
      case Success(profile) => // "code block"
        // TODO with steps 2 and 3
        val friendProfileFuture = SocialNetwork.fetchBestFriend(profile)
        friendProfileFuture.onComplete {
          case Success(friendProfile) => profile.sendMessage(friendProfile, message)
          case Failure(e) => e.printStackTrace()
        }
      case Failure(ex) => ex.printStackTrace()
    }
  }

  // onComplete is a hassle.
  // solution: functional composition


  def sendMessageToBestFriend_v2(accountId: String, message: String): Unit = {
    val profileFuture = SocialNetwork.fetchProfile(accountId)
    val action = profileFuture.flatMap { profile => // Future[Unit]
      SocialNetwork.fetchBestFriend(profile).map { bestFriend => // Future[Unit]
        profile.sendMessage(bestFriend, message) // unit
      }
    }
  }

  def sendMessageToBestFriend_v3(accountId: String, message: String): Unit =
    for {
      profile <- SocialNetwork.fetchProfile(accountId)
      bestFriend <- SocialNetwork.fetchBestFriend(profile)
    } yield profile.sendMessage(bestFriend, message) // identical to v2

  val janeProfileFuture = SocialNetwork.fetchProfile("rtjvm.id.1-jane")
  val janesFuture: Future[String] = janeProfileFuture.map(profile => profile.name) // map transforms value contained inside, ASYNCHRONOUSLY
  val janesBestFriend: Future[Profile] = janeProfileFuture.flatMap(profile => SocialNetwork.fetchBestFriend(profile))
  val janesBestFriendFilter: Future[Profile] = janeProfileFuture.filter(profile => profile.name.startsWith("2"))

  // fallbacks
  val profileNoMatterWhat = SocialNetwork.fetchProfile("unknown id").recover {
    case e: Throwable => Profile("rtjvm.id.1-jane", "Forever alone")
  }

  val aFetchedProfileNoMatterWhat: Future[Profile] = SocialNetwork.fetchProfile("unknown id").recoverWith {
    case e: Throwable => SocialNetwork.fetchProfile("rtjvm.id.0-dummy")
  }

  val fallBackProfile: Future[Profile] = SocialNetwork.fetchProfile("unknown id").fallbackTo(SocialNetwork.fetchProfile("rtjvm.id.0-dummy"))

  /*
    Block for a future
   */
  case class User(name: String)
  case class Transaction(sender: String, receiver: String, amount: Double, status: String)

  object BankingApp {
    // "APIs"
    def fetchUser(name: String): Future[User] = Future {
      // simulate some DB fetching
      Thread.sleep(500)
      User(name)
    }

    def createTransaction(user: User, merchantName: String, amount: Double): Future[Transaction] = Future {
      // simulate payment
      Thread.sleep(1000)
      Transaction(user.name, merchantName, amount, "SUCCESS")
    }

    // "external API"
    def purchase(username: String, item: String, merchantName: String, price: Double): String = {
      /*
        1. fetch user
        2. create transaction
        3. WAIT for the transaction to finish
       */
      val transactionStatusFuture: Future[String] = for {
        user <- fetchUser(username)
        transaction <- createTransaction(user, merchantName, price)
      } yield transaction.status

      // blocking call
      Await.result(transactionStatusFuture, 2.seconds) // throws TimeoutException if the future doesn't finish within 2s
    }
  }

  /*
    Promises
   */
  def demoPromises(): Unit = {
    val promise = Promise[Int]()
    val futureInside: Future[Int] = promise.future

    // thread 1 - "consumer": monitor the future for completion
    futureInside.onComplete {
      case Success(value) => println(s"[consumer] I've just been completed with $value")
      case Failure(ex) => ex.printStackTrace()
    }

    // thread 2 - "producer"
    val producerThread = new Thread(() => {
      println("[producer] Crunching numbers...")
      Thread.sleep(1000)
      // "fulfill" the promise
      promise.success(42)
      println("[producer] I'm done")
    })

    producerThread.start()
  }

  /**
    Exercises
    1. fulfill a future IMMEDIATELY with a value
    2. in sequence: make sure the first future has been completed before returning the second
    3. first(fa, fb) => new future with the value of the first Future to complete
    4. last(fa, fb) => new future with the value of the last Future to complete
    5. retry an action returning a future until a predicate holds true
   */

  // 1
  // I assume that we're talking about a future that is controllable via a promise
  // Otherwise can we do it?
//  def instantFutureFulfillment(): Unit = {
//    val promise = Promise[Int]
//    val future = promise.future
//    promise.success(234)
//
//    future.onComplete {
//      case Success(value) => println(s"Future fulfilled with value $value")
//      case Failure(ex) => println(s"Future failed with exception ${ex.getStackTrace}")
//    }
//  }
  // Apparently all assumptions were wrong. Overcomplicated it.

  // Daniel's solution
  // The future will store this value as soon as possible
  // So: as soon as a thread is scheduled ON THE EXECUTION CONTEXT
  // the future will store this value without any additional computation
  def completeImmediately[A](value: A): Future[A] = Future(value) // async completion asap
  /* Conclusions:
    1. value received as parameter -> makes sense
    2. type of value passed as a generic type for this method -> for the sake of generality
    3. returning a future that has its value already there, no computation needed -> makes sense also
    4. The challange was in a. understanding the question b. familiarity with futures and terminology
   */
  def completeImmediately_v2[A](value: A): Future[A] = Future.successful(value) // a synchronous compoletion
  // first one does NOT guarantee Future is completed at the point of calling
  // second one has stricteed guarantees - the Future.apply can take basically anything -> hence the lack of guarantee from above

  // 2
  def inSequence[A, B](first: Future[A], second: Future[B]): Future[B] = {
    // Does not compile:
    /*
    missing argument for parameter f of method onComplete in trait Future: (f: scala.util.Try[A] => U)
      (implicit executor: scala.concurrent.ExecutionContext): Unit
        first.onComplete() {
     */
    //    first.onComplete() {
//      case Success(a) => second
//    }
    first.flatMap(_ => second)
  }

  // 3
//  def first[A](f1: Future[A], f2: Future[A]): Future[A] = {
//    var finishedFirst = true
//
//    f1.onComplete() {
//      case Success(_) => {
//        if (finishedFirst) {
//          finishedFirst = false
//          return f1 // non functional
//        }
//      }
//    }
//
//    f2.onComplete() {
//      case Success(_) => {
//        if (finishedFirst) {
//          finishedFirst = false
//          return f2 // non functional
//        }
//      }
//    }
//  }

  def first_Daniel[A](f1: Future[A], f2: Future[A]): Future[A] = {
    val promise = Promise[A]()
    f1.onComplete(result1 => promise.tryComplete(result1)) // complete cannot be called twice
    f2.onComplete(result2 => promise.tryComplete(result2)) // and here it was guaranteed to be called twice. tryComplete comes to the rescue

    promise.future
  }

  // 4
//  def last[A](f1: Future[A], f2: Future[A]): Future[A] = {
//    var finishedFirst = true
//
//    f1.onComplete() {
//      case Success(_) => {
//        if (finishedFirst) {
//          finishedFirst = false
//          f2
//        }
//      }
//    }
//
//    f2.onComplete() {
//      case Success(_) => {
//        if (finishedFirst) {
//          finishedFirst = false
//          f1
//        }
//      }
//    }

    // Useful to know for the order of completion of Future
    // in the absence of FP primitives
    def last_Daniel[A](f1: Future[A], f2: Future[A]): Future[A] = {
      val bothPromise = Promise[A]()
      val lastPromise = Promise[A]()

      // any of the above futures can finish with Try[A]
      def checkAndComplete(result: Try[A]): Unit =
        if (!bothPromise.tryComplete(result))
          lastPromise.complete(result)

      f1.onComplete(checkAndComplete)
      f2.onComplete(checkAndComplete)

      lastPromise.future
  }

  // 5 retry an action returning a future until a predicate holds true
//  def retryUntil[A](action: () => Future[A], predicate: A => Boolean): Future[A] = {
//    action.apply().onComplete() {
//      case Success(result) =>
//        if (!predicate(result)) {
//          println("Retry. predicate(result) was False")
//          retryUntil(a, predicate)
//        }
//        else {
//          println("Finally, predicate(result) was true and returned future")
//          action
//        }
//    }
//  }

  def retryUntil_Daniel[A](action: () => Future[A], predicate: A => Boolean): Future[A] = {
    action()
      .filter(predicate)
      .recoverWith {
        case _ => retryUntil_Daniel(action, predicate)
      }
  }

  def testRetries(): Unit = {
    val random = new Random()
    val action = () => Future {
      Thread.sleep(100)
      val nextValue = random.nextInt(100)
      println(s"Generated $nextValue")
      nextValue
    }

    val predicate = (x: Int) => x < 10

    retryUntil_Daniel(action, predicate).foreach(finalResult => println(s"Settled at $finalResult"))

  }

  def testFirstLast(): Unit = {
    // Daniel test first & last
    lazy val fast = Future {
      Thread.sleep(100)
      1
    }
    lazy val slow = Future {
      Thread.sleep(200)
      2
    }
    first_Daniel(fast, slow).foreach(result => println(s"FIRST: $result"))
    last_Daniel(fast, slow).foreach(result => println(s"LAST: $result"))
  }

  def main(args: Array[String]): Unit = {
//    println(futureInstantResult) // inspect the value of the future RIGHT NOW
//    sendMessageToBestFriend_v3("rtjvm.id.1-jane", "Hey, best friend, nice to talk to you again!")
//    println("purchasing")
//    println(BankingApp.purchase("daniel-234", "shoes", "merchant-97", 3.56))
//    println("purchase complete")

//    demoPromises()

//    instantFutureFulfillment()

//    testFirstLast()

    testRetries()

//    val random = Random()
//    val delayedRandomIntFuture: Future[Int] = Future.apply {
//      Thread.sleep(300)
//      random.nextInt(100)
//    }
//
//    retryUntil(delayedRandomIntFuture, (x: Int) => Boolean = _ % 5 == 0)
    Thread.sleep(2000)
    executor.shutdown()
  }
}
