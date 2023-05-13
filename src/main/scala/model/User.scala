package model

case class User(login: String, firstName: String, lastName: String){

  def ifExists(another: User) =
    this.login == another.login

  override def equals(obj: Any): Boolean = obj match {
    case User(`login`, _, _) =>  true
    case _ => false
  }

  override def hashCode(): Int = login.hashCode
}