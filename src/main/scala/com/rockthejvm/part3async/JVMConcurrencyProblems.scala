package com.rockthejvm.part3async

object JVMConcurrencyProblems {

  def runInParallel(): Unit = {
    var x = 0

    val thread1 = new Thread(() => {
      x = 1
    })

    val thread2 = new Thread(() => {
      x = 2
    })

    thread1.start()
    thread2.start()
//    Thread.sleep(100)
    println(x) // race condition
  }

  case class BankAccount(var amount: Int)

  def buy(bankAccount: BankAccount, thing: String, price: Int): Unit = {
    bankAccount.amount -= price
  }

  def buySafe(bankAccount: BankAccount, thing: String, price: Int): Unit = {
    // made this block ATOMIC
    bankAccount.synchronized { // does not allow multiple threads to run the critical section AT THE SAME TIME
      bankAccount.amount -= price // critical section
    }
  }
  /*
    Example race condition:
    thread1 (shoes)
      - reads amount 50000
      - compute result 50000 - 3000 = 47000
    thread2 (iPhone)
      - reads amount 50000
      - compute result 50000 - 4000 = 46000
    thread1 (shoes)
      - write amount 47000
    thread2 (iPhone)
      - write amount 46000
   */
  def demoBankingProblem(): Unit = {
    (1 to 100000).foreach { _ =>
      val account = BankAccount(50000)
      val thread1 = new Thread(() => buy(account, "shoes", 3000))
      val thread2 = new Thread(() => buy(account, "iPhone", 4000))
      thread1.start()
      thread2.start()
      thread1.join()
      thread2.join()
      if (account.amount != 43000) println(s"AHA, I've just broken the bank: ${account.amount}")
    }
  }

  /**
    Exercises
    1 - create "inception threads"
      thread 1
        -> thread 2
            -> thread 3
                ....
      each thread prints "hello from thread $i"
      Print all messages IN REVERSE ORDER
   2 - what's the max/min value of x?
   3 - "sleep fallacy": what's the value of message?
  */

  // 1 - inception threads
  def launchInceptionThreads(maxThreads: Int) = {
    def inceptionRunnable(id: Int): Unit ={
      println(s"hello from thread ${id + 1}")
    }

    def launchInceptionThread(idx: Int): Unit = {
      if (idx < maxThreads) {
        launchInceptionThread(idx + 1)
        val thread = new Thread(() => inceptionRunnable(idx))
        thread.start()
        thread.join() // wait until it is closed
      }
    }

    launchInceptionThread(0)
  }

  // Daniel's solution
  def inceptionThreads(maxThreads: Int, i: Int = 1): Thread =
    new Thread(() => {
      if (i < maxThreads) {
        val newThread = inceptionThreads(maxThreads, i + 1)
        newThread.start()
        newThread.join()
      }
      println(s"hello from thread ${i}")
    })
  // remarks:
  // 1. I can ask myself if I can use only 1 method
  // 2. that returns a thread
  // 3. and ofc do print action recursively, like in postfix tree traversals

  // 2 -> min: 1, max: 100
  def minMax(): Unit = {
    var x = 0
    val threads = (1 to 100).map(_ => new Thread(() => x += 1))
    threads.foreach(_.start())
  }

  // 3 - "Scala is awesome" - most of the time -> not guaranteed
  def demoSleepFallacy(): Unit = {
    var message = ""
    val awesomeThread = new Thread(() => {
      Thread.sleep(1000)
      message = "Scala is awesome"
    })

    message = "Scala sucks"
    awesomeThread.start()
    Thread.sleep(1001)
    // solution: join the worker thread
    awesomeThread.join()
    println(message)
  }

  def main(args: Array[String]): Unit = {
//    runInParallel()
//    demoBankingProblem()
    launchInceptionThreads(5)
    inceptionThreads(10).start()
  }
}
