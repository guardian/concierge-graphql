package datastore

import security.UserTier

case class GQLQueryContext(repo: DocumentRepo, userTier: UserTier)
