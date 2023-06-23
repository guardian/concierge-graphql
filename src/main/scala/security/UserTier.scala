package security

sealed trait UserTier {
  def toString():String

  /**
   * returns true if this permission level is less than the "other" permission level, false if this is greater than or equal to
   * @param other other permission level to compare to
   * @return
   */
  def < (other:UserTier):Boolean
}

case object DeveloperTier extends UserTier {
  override def toString() = "developer"

  override def <(other: UserTier): Boolean = other!=DeveloperTier
}

case object RightsManagedTier extends UserTier {
  override def toString() = "rights-managed"

  override def <(other: UserTier): Boolean = ! (other==RightsManagedTier || other==DeveloperTier)
}

case object ExternalTier extends UserTier {
  override def toString() = "external"

  override def <(other: UserTier): Boolean = ! (other==RightsManagedTier || other==DeveloperTier || other==ExternalTier)
}

case object InternalTier extends UserTier {
  override def toString() = "internal"

  override def <(other: UserTier): Boolean = false
}

object UserTier {
  def apply(from:String):Option[UserTier] = from match {
    case "developer"=>Some(DeveloperTier)
    case "rights-managed"=>Some(RightsManagedTier)
    case "external"=>Some(ExternalTier)
    case "internal"=>Some(InternalTier)
    case _=>None
  }
}