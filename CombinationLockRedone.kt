interface ICombinationLock {

    val health: Int

    val locked: Boolean

    val combination: IntArray

    val unlockAttempts: Int

    val pickAttempts: Int

    var min: Int

    var max: Int

    val adminMode: Boolean

    val password: String

    val broken: Boolean

    fun damageLock(): Int

    fun lock(): Boolean

    fun unlock(combination: IntArray): Boolean

    fun pick(): Boolean

    fun adminMode(password: String): Boolean

    fun leaveAdmin()

    fun resetHealth()

    fun setCombination(combination: IntArray): Boolean

    fun save(filename: String): Boolean

    fun load(filename: String): Boolean

}