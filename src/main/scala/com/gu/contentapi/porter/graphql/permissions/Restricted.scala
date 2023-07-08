package com.gu.contentapi.porter.graphql.permissions

import sangria.execution.FieldTag
import security.UserTier

case class Restricted(to:UserTier) extends FieldTag
