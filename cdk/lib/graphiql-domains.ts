export const graphiqlDomains = {
  "PROD-AARDVARK": "graphiql.aardvark.guexperiments.co.uk",
  "CODE-AARDVARK": "graphiql.code-aardvark.guexperiments.co.uk",
  "PROD-ZEBRA": "graphiql.zebra.guexperiments.co.uk",
  "CODE-ZEBRA": "graphiql.code-zebra.guexperiments.co.uk",
} as const;

export type GraphiqlStage = keyof typeof graphiqlDomains;