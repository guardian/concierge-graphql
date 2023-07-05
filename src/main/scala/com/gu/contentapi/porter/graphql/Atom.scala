package com.gu.contentapi.porter.graphql

import sangria.schema._
import sangria.macros.derive._
import com.gu.contentapi.porter.model

object Atom {
  val SimpleAtom = deriveObjectType[Unit, model.SimpleAtom]()
}
