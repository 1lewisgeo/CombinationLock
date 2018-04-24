import java.io.*
import java.util.*

val random = Random()
/**
 * Get a random number between min and max (inclusive, inclusive)
 * @return a pseudorandom number
 */
fun rand(max: Int, min: Int = 0): Int = random.nextInt((max+1) - min) + min


/**
 * get the object output stream of a file as an extension function
 * @return an object output stream for this file
 */
fun File.ObjectOutputStream(): ObjectOutputStream = ObjectOutputStream(outputStream())

/**
 * get the object input stream of a file as an extension function
 * @return an object input stream for this file
 */
fun File.ObjectInputStream(): ObjectInputStream = ObjectInputStream(inputStream())

/**
 * An encapsulated class representing a combination lock
 * @constructor Construct a new instance with the specified combination or a randomized one
 */
class CombinationLock(private var combination_: IntArray = IntArray(3, { rand(10) })) : ICombinationLock {

    /**
     * Run with constructors, checks for minimum combination length
     */
    init {
        if (combination_.size < 3) {
            throw InstantiationException("CombinationLock must be instantiated with a combination of at least length 3")
        }
    }

    val size
        /**
         * The size of the combination
         * @return an integer representing the size of the combination
         */
    get() = combination_.size

    /**
     * The lock's combination
     */
    override var combination: IntArray = combination_
        private set(value) {
            field = value
            combination_ = value
        }
        get() = if (adminMode and (!broken)) field else IntArray(field.size, { -1 })

    /**
     * The health value of the lock on the interval [0, 100]
     */
    override var health: Int = 100
        private set

    /**
     * The locked status of the lock, true is locked, false is unlocked
     */
    override var locked: Boolean = true
        private set

    /**
     * The number of times an attempt to unlock the lock has been made
     */
    override var unlockAttempts: Int = 0
        private set

    /**
     * The number of times an attempt to pick the lock has been made
     */
    override var pickAttempts: Int = 0
        private set

    /**
     * The minimum value for each digit of the lock
     */
    override var min = 0

    /**
     * The maximum vallue for each digit of the lock
     */
    override var max = 9

    /**
     * The admin status of the lock, true is admin mode enabled, false is admin mode disabled
     */
    override var adminMode: Boolean = false
        private set

    /**
     * The admin password for the lock. By default "DEFAULT".
     */
    override var password: String = "DEFAULT"
        set(value) {
            if (adminMode and !broken) {
                field = value
                this.leaveAdmin()
            }
        }

    /**
     * The broken status of the lock, true indicates a broken lock
     */
    override val broken: Boolean
        get() = health <= 0

    /**
     * Damages the lock by a random value between [4, 7]
     * @return returns the new health of the lock
     */
    override fun damageLock(): Int = if (!broken && health > 0) run { health = health - 4 - rand(3); health = if (health < 0) 0 else health; health } else health

    /**
     * Locks the lock
     */
    override fun lock() = if (!broken) run { locked = true; true } else false

    override fun unlock(combination: IntArray): Boolean {
        if (!broken && locked) {
            unlockAttempts++
            locked = !(this.combination_ contentEquals combination)
            return !locked
        } else {
            return false
        }
    }

    /**
     * Attempts to pick the lock
     * @return returns true if picking the lock was successful
     */
    override fun pick(): Boolean {
        if (!broken && locked) {
            pickAttempts++
            locked = rand(100) !in 0..20
            return !locked
        } else {
            return false
        }
    }

    /**
     * Enter admin mode using the password
     * @param password The admin password
     * @return returns true if admin mode was entered, false otherwise
     */
    override fun adminMode(password: String): Boolean = if (this.password == password) run { adminMode = true; true } else run { adminMode = false; false }

    /**
     * Disables admin mode
     */
    override fun leaveAdmin() = run { adminMode = false }

    /**
     * Admin only. Resets the health of the lock to 100
     */
    override fun resetHealth() = if (adminMode) run { health = 100 } else {}

    /**
     * Admin only. Saves the lock to a file
     * @param filename a valid filename / path to save the lock to according to [File]
     * @return returns true if saving the lock was successful and false if it was not
     */
    override fun save(filename: String): Boolean {
        if (adminMode) {
            try {
                File(filename).ObjectOutputStream().use {
                    for (x in listOf(
                            combination,
                            health,
                            locked,
                            unlockAttempts,
                            pickAttempts,
                            password,
                            min,
                            max
                    )) {
                        it.writeObject(x)
                    }
                }
            } catch (e: IOException) {
                return false
            }
        }
        return true
    }

    /**
     * Admin only. Loads the lock from a file
     * @param filename a valid filename / path to load the lock from according to [File]
     */
    override fun load(filename: String): Boolean {
        if (adminMode) {
            try {
                File(filename).ObjectInputStream().use {
                    for (i in 0..7) {
                        val value = it.readObject()
                        when (i) {
                            0 -> combination = value as IntArray
                            1 -> health = value as Int
                            2 -> locked = value as Boolean
                            3 -> unlockAttempts = value as Int
                            4 -> pickAttempts = value as Int
                            5 -> password = value as String
                            6 -> min = value as Int
                            7 -> max = value as Int
                        }
                    }
                }
            } catch (e: IOException) {
                return false
            }
        }
        return true
    }

    /**
     * Admin only. Sets the commbination to a new value
     * @param combination the new combination for the lock
     * @return returns true if setting the new combination was successful and false otherwise
     */
    override fun setCombination(combination: IntArray): Boolean {
        if (!broken and adminMode) {
            if (combination.all { it in min..(max+1) } && combination.size >= 3) {
                this.combination = combination
                lock()
                return true
            } else {
                return false
            }
        } else {
            return false
        }
    }

    /**
     * Validates the combination based upon the min and max values
     * @return returns true if the combination is valid, false otherwise
     */
    fun validateCombination() = combination_.all { it >= min && it <= max }

    override fun toString(): String {
        return "{Combination : ${Arrays.toString(combination_)}, Health : $health}"
    }

}

fun main (args: Array<String>) {
    var lock = CombinationLock()
    lock.damageLock()
    lock.password = "ABC"
    println(lock)
    lock.save("lock.lck")
    lock = CombinationLock()
    println(lock)
    lock.load("lock.lck")
    println(lock)
}