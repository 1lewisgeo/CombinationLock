import java.util.*
import kotlin.test.assertFalse
import kotlin.test.assertTrue

fun main(args: Array<String>) {

    var lock = CombinationLock(intArrayOf(7, 3, 5, 2, 9, 5))

    assert(lock.size == 6) // Check that we have the right number of digits

    assert(lock.unlockAttempts == 0) // We have made no attempts to unlock the lock; attempts should be 0

    assert(lock.pickAttempts == 0) // We have made no pick attempts; should be 0

    assert(lock.unlock(intArrayOf(7, 3, 5, 2, 9, 5))) // Unlocking should work using set combination

    assert(lock.unlockAttempts == 1) // We have made one unlock attempt

    assertTrue {
        val hp = lock.health
        lock.damageLock()
        hp > lock.health
    }

    assert(lock.lock()) // The lock should now be locked

    assertTrue {
        val startAttempts = lock.pickAttempts
        var counter = 0
        while (lock.locked) {
            lock.pick()
            counter++
        }
        !lock.locked && (lock.pickAttempts - startAttempts) == counter
    }

    assert(lock.lock()) // The lock should now be locked

    // Using the algorithm copied from lock client

    /**
     * Brute forces the lock by testing all combinations sequentially
     */
    fun bruteforce(lock: CombinationLock) {

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

                    return

                }

            }

        }

    }

    assertTrue {
        val startAttempts = lock.unlockAttempts
        bruteforce(lock)
        !lock.locked && (lock.unlockAttempts - startAttempts) == 735295
    }

    assert(lock.lock()) // The lock should now be locked

    assert(!lock.adminMode) // We should not be in admin mode

    assert(lock.adminMode("DEFAULT")) // Enter admin mode and assert that it was successful

    assert(lock.combination contentEquals intArrayOf(7, 3, 5, 2, 9, 5)) // Combination should be what we initialized the lock with

    assert(lock.setCombination(intArrayOf(2, 5, 7))) // Change lock combination and assert success

    assert(lock.combination contentEquals intArrayOf(2, 5, 7)) // Check that the combination was changed

    assert(lock.size == 3) // Check that the combination is the right length

    assert(lock.locked) // Check that the lock is locked after changing the combination

    assertFalse { lock.setCombination(intArrayOf(-1, -5, 4)) } // Attempt to set combination with values below the minimum (default 0); this should not work

    assertFalse { lock.combination contentEquals intArrayOf(-1, -5, 4) } // Ensure that the combination did not get changed

    assertFalse { lock.setCombination(intArrayOf(10, 5, 12)) } // Attempt to set combination with values above the maximum (default 9); this should not work

    assertFalse { lock.combination contentEquals intArrayOf(10, 5, 12) } // Ensure that the combination did not get changed

    assert(lock.min == 0) // Check default

    assert(lock.setMin(2)) // Change min

    assert(lock.min == 2) // Check that the value stuck

    assert(lock.setCombination(intArrayOf(2, 2, 2))) // Should work; above new minimum

    assertFalse { lock.setCombination(intArrayOf(0, 5, 2)) } // Should fail! 0 is now below the minimum

    assert(lock.max == 9) // Check default

    assert(lock.setMax(7)) // Change max

    assert(lock.max == 7) // Check max for new value

    assert(lock.setCombination(intArrayOf(2, 5, 7))) // Should work; all values between new min and new max

    assertFalse { lock.setCombination(intArrayOf(0, 5, 9)) } // This combination would work with default values but should fail now due to new max & min

    lock.leaveAdmin() // Leave admin mode

    assertFalse { lock.adminMode } // Check that we are no longer in admin mode

    assert(lock.adminMode("DEFAULT")) // Reenter admin

    lock.password = "qwerty" // Change password

    assertFalse { lock.adminMode } // As our last entered password is now incorrect we should not be in admin mode

    assert(lock.adminMode("qwerty")) // Enter admin with new password

    assert(lock.toString() == ("{Combination : [2, 5, 7], Health : ${lock.health}}")) // Check that toString is as expected

    assertFalse {

        val combination = lock.combination // Store current combination

        // Algorithm taken from lock client

        lock.setCombination(IntArray(lock.size, { rand(lock.max, lock.min) })) // Randomize!

        combination contentEquals lock.combination // Check that the combinations differ

    }

    run {

        // Save the data from the lock prior to save to compare to loaded lock

        val combination = lock.combination
        val hp = lock.health
        val locked = lock.locked
        val unlockAttempts = lock.unlockAttempts
        val pickAttempts = lock.pickAttempts
        val password = lock.password
        val min = lock.min
        val max = lock.max

        // Save the lock

        assert(lock.save("lock.lck"))

        // Create a fresh lock

        lock = CombinationLock(intArrayOf(5, 6, 0))

        // Assert a few default values

        assertFalse { combination contentEquals lock.combination }

        assertFalse { hp == lock.health }

        assert( lock.locked )

        assert(lock.unlockAttempts == 0 && lock.pickAttempts == 0)

        assert(lock.adminMode("DEFAULT"))

        // Load the lock

        assert(lock.load("lock.lck"))

        // Enter admin mode so we can check all the values | also check if password persisted

        assert(lock.adminMode(password))

        assert(lock.password == password)

        // Check that the values persisted

        assert(lock.combination contentEquals combination)

        assert(lock.health == hp)

        assert(lock.locked == locked)

        assert(lock.unlockAttempts == unlockAttempts)

        assert(lock.pickAttempts == pickAttempts)

        assert(lock.min == min)

        assert(lock.max == max)

        //

    }

}