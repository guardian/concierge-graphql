package com.gu.contentapi.porter.graphql

import sangria.schema._

object TagQueryParameters {
  import PaginationParameters._

  val AlternateIdTypes = EnumType(
    "AlternateIdType",
    Some("Different types of alternate IDs available"),
    List(
      EnumValue("all",
        value = "all",
        description = Some("Return all alternate IDs"),
      ),
      EnumValue("url",
        value = "urlpath",
        description = Some("The full URL reference")
      ),
      EnumValue("shortcode",
        value = "shortcode",
        description = Some("URL short-code")
      ),
      EnumValue("internalComposer",
        value = "composer",
        description = Some("Internal composer code reference")
      ),
      EnumValue("internalPage",
        value = "page",
        description = Some("Internal page code reference")
      ),
      EnumValue("internalOctopus",
        value = "octopus",
        description = Some("Internal print system reference")),
      EnumValue("internalPluto",
        value = "pluto",
        description = Some("Internal video production system reference"))
    )
  )

  val TagTypes = EnumType(
    "TagType",
    Some("Different types of tag available"),
    List(
      EnumValue("contributor",
        value = "contributor",
        description = Some("Tags which describe an author or co-author")
      ),
      EnumValue("keyword",
        value = "keyword",
        description = Some("Tags which are used to organise and categorise content")
      ),
      EnumValue("series",
        value = "series",
        description = Some("Tags which are used to group content which belongs in a series")
      ),
      EnumValue("podcast",
        value = "podcast",
        description = Some("A series which is also a podcast")
      ),
      EnumValue("newspaperBookSection",
        value = "newspaper-book-section",
        description = Some("Tags used for internal organisation of the newspaper")
      ),
      EnumValue("newspaperBook",
        value = "newspaper-book",
        description = Some("Tags used for internal organisation of the newspaper")
      ),
      EnumValue("blog",
        value = "blog",
        description = Some("Tags which identify something as a blog")
      ),
      EnumValue("paidContent",
        value = "paid-content",
        description = Some("Tags which identify something as having been externally paid for")
      ),
      EnumValue("campaign",
        value = "campaign",
        description = Some("Tags which identify content belonging to a campaign")
      ),
      EnumValue("tone",
        value = "tone",
        description = Some("Tags which identify the intention of the content")
      ),
      EnumValue("type",
        value = "type",
        description = Some("Tags which identify the kind of content")
      ),
      EnumValue("tracking",
        value = "tracking",
        description = Some("Tags which are used for tracking content")
      ),
      EnumValue("publication",
        value = "publication",
        description = Some("Tags to identify the publication that this was commissioned for"))
    )
  )

  val FuzzinessOptions = EnumType(
    "FuzzinessOptions",
    Some("Valid options for making a fuzzy-match query"),
    List(
      EnumValue("AUTO",
        value="AUTO",
        description=Some("Generates an edit distance based on the length of the term. If the term is >5 chars, then 2 edits allowed; if <3 chars than no edits allowed")
      ),
      EnumValue("OFF",
        value="OFF",
        description=Some("Disable fuzzy-matching")
      )
    )
  )

  val tagId = Argument("tagId", OptionInputType(StringType), description = "Retrieve this specific tag")
  val Section = Argument("section", OptionInputType(StringType), description = "Only return tags from this section")
  val TagType = Argument("type", OptionInputType(TagTypes), description = "Type of the tag to return")
  val QueryString = Argument("q", OptionInputType(StringType), description = "Search for tags that match this public-facing name")
  val Fuzziness = Argument("fuzzy", OptionInputType(FuzzinessOptions), description = "Perform a fuzzy-matching query (default). Set to `OFF` to disable fuzzy-matching.")
  val Category = Argument("category", OptionInputType(StringType), description = "A category to match against tags")
  val Reference = Argument("reference", OptionInputType(StringType), description = "A reference to match against tags")
  val AllTagQueryParameters = QueryString :: tagId :: Section :: TagType :: Fuzziness :: Category ::
    Reference :: Cursor :: OrderBy :: Limit :: Nil

  val NonPaginatedTagQueryParameters = QueryString :: tagId :: Section :: TagType :: Fuzziness :: Category ::
    Reference :: Nil
}
