import java.io.File
import java.util.*

fun main(args: Array<String>) {

    // The lock
    val lock = CombinationLock()

    println("Welcome to the Lock Client")

    // Main loop
    outer@ while (true) {

        // CLIENT MENU OUTPUT

        print("====================LOCK====================\n" +
                "| STATUS: ${if (lock.broken) "Broken" else (if (lock.locked) "Locked" else "Unlocked")}\n" +
                "| HEALTH: ${lock.health}\n" +
                "| UNLOCK ATTEMPTS: ${lock.unlockAttempts}\n" +
                "| PICK ATTEMPTS: ${lock.pickAttempts}\n" +
                "| ADMIN MODE: ${if (lock.adminMode) "ENABLED" else "DISABLED"}\n" +
                "============================================\n")

        if (lock.broken || !lock.validateCombination()) {
            print("==================COMMANDS==================\n" +
                    "| 5. ENTER ADMIN\n" +
                    "| 7. EXIT (Exit the program)\n" +
                    "============================================\n")
        } else {
            print("==================COMMANDS==================\n" +
                    "| 1. UNLOCK (Attempt to unlock the lock with a combination)\n" +
                    "| 2. LOCK (Lock the lock)\n" +
                    "| 3. ATTACK (Damage the lock physically)\n" +
                    "| 4. PICK (Attempt to pick the lock)\n" +
                    "| 5. ENTER ADMIN\n" +
                    "| 6. BRUTEFORCE (Attempt every combination on the lock)\n" +
                    "| 7. EXIT (Exit the program)\n" +
                    "============================================\n")
        }

        if (lock.adminMode) {
            if (lock.broken) {
                println("===================ADMIN====================\n" +
                        "| 12. RESET HEALTH (Resets the lock's health)\n" +
                        "============================================\n")
            } else if (!lock.validateCombination()) {
                println("===================ADMIN====================\n" +
                        "| 9. SET COMBINATION (Set the lock's combination)\n" +
                        "| 13. SET MIN (Set the lock's minimum value)\n" +
                        "| 14. SET MAX (Set the lock's maximum value)\n" +
                        "============================================\n")
            } else {
                print("===================ADMIN====================\n" +
                        "| 8. SHOW COMBINATION (Display the lock's combination)\n" +
                        "| 9. SET COMBINATION (Set the lock's combination)\n" +
                        "| 10. SAVE (Save the lock)\n" +
                        "| 11. LOAD (Load a lock)\n" +
                        "| 12. RESET HEALTH (Resets the lock's health)\n" +
                        "| 13. SET MIN (Set the lock's minimum value)\n" +
                        "| 14. SET MAX (Set the lock's maximum value)\n" +
                        "| 15. LEAVE ADMIN (Leave admin mode)\n" +
                        "| 16. RANDOMIZE COMBINATION\n" +
                        "| 17. CHANGE ADMIN PASSWORD\n" +
                        "| 18. TOSTRING\n" +
                        "============================================\n")
            }
        }

        // INPUT VALIDATION

        if (!lock.validateCombination()) {
            println("NOTE: LOCK COMBINATION IS INVALID ACCORDING TO NEW MIN / MAX; MUST BE SET A NEW COMBINATION")
        }

        val input = try {
            readLine()?.toInt()
        } catch (e: NumberFormatException) {
            continue@outer
        }

        if (input!! < 1 || input > 18) {
            println("Invalid input!")
            continue@outer
        }

        if (input >= 8 && !lock.adminMode) {
            println("You cannot use admin functions without activating admin mode")
            continue@outer
        }

        if (lock.broken && input !in intArrayOf(5, 12)) {
            println("The lock is broken! You cannot use this function before fixing the lock")
            continue@outer
        }

        if (!lock.validateCombination() && input !in intArrayOf(5, 9, 7, 13, 14)) {
            println("You must change the combination")
            continue@outer
        }

        // INPUT ACTION

        when (input) {
            1 -> {

                if (!lock.locked) {
                    println("The lock is already unlocked")
                } else {

                    println("Enter combination separated by spaces (Min: ${lock.min}, Max: ${lock.max}, Length: ${lock.size})")
                    val combination: IntArray? = readLine()?.let {
                        it.split(" ").map {
                            try {
                                it.toInt()
                            } catch (e: NumberFormatException) {
                                println("Error in input")
                                return@let null
                            }
                        }.toIntArray()
                    }

                    combination?.let {
                        if (lock.unlock(it)) {
                            println("Unlocked successfully")
                        } else {
                            println("Unlock failure")
                        }
                    }
                }
            }
            2 -> {
                lock.lock()
                println("The lock has been locked")
            }
            3 -> println("Lock attacked! ${lock.damageLock()} health remaining")
            4 -> {
                if (lock.pick()) {
                    println("Lock picked!")
                } else {
                    println("Lock pick failure")
                }
            }
            5 -> {
                if (lock.adminMode) {
                    println("You are already in admin mode")
                } else {
                    println("Enter password")
                    print("Password: ")
                    readLine()?.let {
                        if (lock.adminMode(it)) {
                            println("Authentication success")
                        } else {
                            println("Authentication failure")
                        }
                    }
                }
            }
            6 -> {

                if (lock.locked) {

                    println("Beginning brute force")

                    val start = lock.unlockAttempts

                    with(lock) {

                        val tryc = IntArray(size, { 0 })

                        while (!tryc.any { it > max }) {

                            tryc[size - 1]++

                            for (x in (size - 1) downTo 0) {
                                if (tryc[x] > max) {
                                    tryc[x] = min
                                    if (x == 0) {
                                        break
                                    } else {
                                        tryc[x - 1]++
                                    }
                                }
                            }

                            if (unlock(tryc)) {

                                println("Brute force successful, took ${unlockAttempts - start} attempts, the combination is ${Arrays.toString(tryc)}")

                                break

                            }

                        }

                        if (lock.locked) {
                            println("BRUTE FORCE FAILURE")
                        }

                    }
                } else {
                    println("The lock is not locked")
                }
            }
            7 -> {
                println("Bye!")
                return@main
            }
            8 -> println("The combination is ${Arrays.toString(lock.combination)}")
            9 -> {
                println("Please enter a new combination with ${lock.size} digits, each digit between ${lock.min} and ${lock.max} and with each digit separated by a space")
                readLine()?.let {
                    it.split(" ").map {
                        try {
                            it.toInt()
                        } catch (e: NumberFormatException) {
                            println("Error in input")
                            return@let null
                        }
                    }.toIntArray().let {
                        if (lock.setCombination(it)) {
                            println("Setting combination successful")
                        } else {
                            println("Setting combination failure; combination unchanged")
                        }
                    }
                }
            }
            10 -> {
                // Save
                println("Please enter a valid filepath (as described at https://docs.oracle.com/javase/8/docs/api/java/io/File.html)")
                readLine()?.let {
                    if (lock.save(it)) {
                        println("Lock saved successfully")
                    } else {
                        println("Failed to save the lock")
                    }
                }
            }
            11 -> {
                // Load
                println("Please enter a valid filepath (as described at https://docs.oracle.com/javase/8/docs/api/java/io/File.html)")
                readLine()?.let {
                    if (lock.load(it)) {
                        println("Lock loaded successfully")
                    } else {
                        println("Failed to load the lock")
                    }
                }
            }
            12 -> { lock.resetHealth(); println("The lock's health has been reset") }
            13 -> {
                // Set min
                println("Please enter a single number to be the minimum value for each number of the lock")
                readLine()?.toInt()?.let { if (lock.setMin(it)) println("Minimum set to $it") else println("Failed to set minimum") }
            }
            14 -> {
                // Set max
                println("Please enter a single number to be the maximum value for each number of the lock")
                readLine()?.toInt()?.let { if (lock.setMax(it)) println("Maximum set to $it") else println("Failed to set maximum") }
            }
            15 -> { lock.leaveAdmin(); println("Left admin mode") }
            16 -> {
                lock.randomizeCombination()
                println("Combination randomized")
            }
            17 -> {
                println("Please enter the new admin password")
                readLine()?.let { lock.password = it; println("Password set successfully") } ?: run { println("Failed to set password") }
            }
            18 -> println(lock.toString())
        }

        // Pause output between commands

        print("Press return to continue...")

        readLine()

    }

}