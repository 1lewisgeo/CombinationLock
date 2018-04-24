import java.io.*
import java.util.*

val random = Random()

fun rand(max: Int, min: Int = 0): Int = random.nextInt(max - min) + min

fun File.ObjectOutputStream(): ObjectOutputStream = ObjectOutputStream(outputStream())

fun File.ObjectInputStream(): ObjectInputStream = ObjectInputStream(inputStream())

class CombinationLock(private var combination_: IntArray = IntArray(3, { rand(10) })) : ICombinationLock {

    init {
        if (combination_.size < 3) {
            throw InstantiationException("CombinationLock must be instantiated with a combination of at least length 3")
        }
    }

    val size
    get() = combination_.size

    override var combination: IntArray = combination_
        set(value) {
            field = value
            combination_ = value
        }
        get() = if (adminMode and (!broken)) field else IntArray(field.size, { -1 })

    override var health: Int = 100
        private set

    override var locked: Boolean = true
        private set

    override var unlockAttempts: Int = 0
        private set

    override var pickAttempts: Int = 0
        private set

    override var min = 0
        /*set(value) {
            if (!broken and value.all { it >= 0 } and adminMode) {
                field = value
            }
        }*/

    override var max = 9
        /*set(value) {
            if (!broken and value.all { it <= 9 } and adminMode) {
                field = value
            }
        }*/


    override var adminMode: Boolean = false
        private set

    override var password: String = "DEFAULT"
        set(value) {
            if (adminMode and !broken) {
                field = value
                this.leaveAdmin()
            }
        }

    override val broken: Boolean
        get() = health <= 0

    override fun damageLock(): Int = if (!broken && health > 0) run { health = health - 4 - rand(3); health = if (health < 0) 0 else health; health } else health

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

    override fun pick(): Boolean {
        if (!broken) {
            pickAttempts++
            if (locked) {
                locked = rand(100) !in 0..20
                return !locked
            } else {
                return false
            }
        } else {
            return false
        }
    }

    override fun adminMode(password: String): Boolean = if (this.password == password) run { adminMode = true; true } else run { adminMode = false; false }

    override fun leaveAdmin() = run { adminMode = false }

    override fun resetHealth() = if (adminMode) run { health = 100 } else {}

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

    override fun setCombination(combination: IntArray): Boolean {
        if (!broken and adminMode) {
            if (combination.all { it in min..(max+1) } && combination.size >= 3) {
                this.combination = combination
                this.combination_ = combination
                lock()
                return true
            } else {
                return false
            }
        } else {
            return false
        }
    }

    fun validateCombination() = combination_.all { it >= min && it <= max }

    override fun toString(): String {
        return "{Combination : ${Arrays.toString(combination_)}, Health : $health}"
        //return mapOf("Combination" to Arrays.toString(combination_), "Health" to health, "Password" to password).toString()
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