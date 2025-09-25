fun myCustomFunction(index: Int){
    Thread.sleep(500)
    println("function number $index")
}

annotation class poyasnenie(val info: String)

@poyasnenie("Создать массив потоков, запустить в них функции")
private fun first() {
    println("=========1 thread=========")
    val myThread = Thread { myCustomFunction(1) }
    myThread.start()
    myThread.join()
}

@poyasnenie("Создать два потока, запустить в них функции")
private fun second() {
    println("=========2 threads=========")
    val myThread2 = Thread { myCustomFunction(2) }
    val myThread3 = Thread { myCustomFunction(3) }
    myThread3.start()
    myThread2.start()

    myThread2.join()
    myThread3.join()
}

@poyasnenie("Создать поток, запустить в нем функцию")
private fun third() {
    println("=========Array of threads=========")
    val arrayOfThread: Array<Thread> = Array(10) { index ->
        Thread { myCustomFunction(index) }
    }
    arrayOfThread.forEach {
        it.start()
//        it.join()
    }
    println("сплю 2 секунды чтобы было красиво")
    Thread.sleep(2000)
}

@poyasnenie("Прервать побочный поток из главного до того, как он завершит свою работу")
private fun fourth() {
    println("=========interrupt=========")
// Прервать побочный поток из главного до того, как он завершит свою работу;
    var counter = 0;
    val myThread4 = Thread {
        try {
            while (!Thread.currentThread().isInterrupted) {
                println("Работаю... $counter")
                Thread.sleep(500)
                counter++
            }

        } catch (e: InterruptedException) {
            println("Поток прерван во время сна")
        }
        println("Поток остановлен")
    }
    myThread4.start()
    Thread.sleep(2500)
    myThread4.interrupt()
}

@poyasnenie("Прервать побочный поток из другого побочного потока до того, " +
        "как они завершат свою работу")
private fun fifth() {
    println("=========interrupt from another thread=========")
    var counter = 0;
    val smth = Thread {
        try {
            while (!Thread.currentThread().isInterrupted) {
                println("smth: работаю... $counter")
                Thread.sleep(1000)
                counter++
            }
        } catch (e: InterruptedException) {
            println("smth: прерван во время сна")
        }
        println("smth: остановлен")
    }

    val killerSmth = Thread {
        Thread.sleep(2500) // ждём немного
        println("Killer: прерываю Worker")
        smth.interrupt()
    }
    smth.start()
    killerSmth.start()
    smth.join()
    killerSmth.join()
}

@poyasnenie("Создать функции <Производитель> и <Потребитель>, " +
        "которые работают с общим массивом (одна функция кладет в него данные, " +
        "другая забирает). Создать несколько потоков-потребителей и " +
        "несколько потоков-производителей")
private fun sixth() {
    println("=====manufacturer and consumer=====")
    val storage = mutableListOf<String>()
    val lock = Object()
    val repeatTimes = 5
    val manufacturerSleep = 25L
    val consumerSleep = 100L

    val manufacturer = Thread {
        repeat(repeatTimes) {
            synchronized(lock) {
                while (storage.size >= 3) {
                    println("manufacturer ждёт")
                    lock.wait()
                }
                storage.add("Товар")
                println("Producer добавил товар (всего: ${storage.size})")
                lock.notifyAll()
            }
            Thread.sleep(manufacturerSleep)
        }
    }
    val consumer = Thread {
        repeat(repeatTimes) {
            synchronized(lock) {
                while (storage.isEmpty()) {
                    println("consumer ждёт")
                    lock.wait()
                }
                storage.removeAt(0)
                println("Consumer забрал товар (осталось: ${storage.size})")
                lock.notifyAll()
            }
            Thread.sleep(consumerSleep)
        }
    }
    consumer.start()
    manufacturer.start()


    manufacturer.join()
    consumer.join()
}

@poyasnenie("Создать побочный поток, который создает другой побочный поток." +
        "Сделать так, чтобы при прерывании первого потока перед своим завершением " +
        "он прервал другой поток")
private fun seventh() {
    println("=====Matryoushka=====")
    val chiefThread = Thread {
        val slaveThread = Thread {
            try {
                while (!Thread.currentThread().isInterrupted) {
                    println("slaveThread работает...")
                    Thread.sleep(500)
                }
            } catch (e: InterruptedException) {
                println("slaveThread прерван во время сна")
            }
            println("slaveThread завершён")
        }

        slaveThread.start()
        var i = 0
        try {
            while (!Thread.currentThread().isInterrupted) {
                println("chiefThread шаг $i")
                i++
                Thread.sleep(500)
            }
        } catch (e: InterruptedException) {
            println("chiefThread прерван во время сна, прерывает slaveThread")
            slaveThread.interrupt()
        }

        // если цикл прерван, всё равно выключаем ребёнка
        if (slaveThread.isAlive) {
            println("chiefThread прерван, прерывает slaveThread")
            slaveThread.interrupt()
        }

        slaveThread.join()
        println("chiefThread завершён")
    }
    chiefThread.start()
    Thread.sleep(2000)
    println("ГЛАВНЫЙ DungeonMaster прерывеает chiefThread")
    chiefThread.interrupt()
    chiefThread.join()
}
fun main(){
//    first()
//    second()
//    third()
//    fourth()
//    fifth()
//    sixth()
//    seventh()
    val sizeOfarray = 10
    val arrayFinish = mutableListOf<String>()
    val runners: Array<Thread> = Array(sizeOfarray) { index ->
        Thread {
            val runTime = (100..110).random().toLong()
            Thread.sleep(runTime)

            println("$index прибыл (время: $runTime мс)")
            synchronized(arrayFinish) {   // синхронизация, чтобы потоки не писали одновременно
                if (arrayFinish.size < 3) {
                    arrayFinish += "$index прибыл (место №${arrayFinish.size+1})"
                }
            }
        }
    }
    runners.forEach { it.start() }
    runners.forEach { it.join() }
    println("Вывод победителей")
    arrayFinish.forEach {
        println(it)
    }





}









