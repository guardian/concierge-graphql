package datastore

import security.UserTier

case class GQLQueryContext(documentRepo: DocumentRepo, frontsRepo: FrontsRepo, userTier: UserTier)
