"use strict";(self.webpackChunkconcierge_graphql_explorer=self.webpackChunkconcierge_graphql_explorer||[]).push([[178],{8009:(e,t,a)=>{a.d(t,{i:()=>r});function r(e,t){var a,r;const{levels:l,indentLevel:n}=e;return((l&&0!==l.length?l.at(-1)-((null===(a=this.electricInput)||void 0===a?void 0:a.test(t))?1:0):n)||0)*((null===(r=this.config)||void 0===r?void 0:r.indentUnit)||0)}(0,Object.defineProperty)(r,"name",{value:"indent",configurable:!0})},4178:(e,t,a)=>{a.r(t);var r=a(7480),l=a(9361),n=a(8009),u=(a(7294),a(5893),a(3935),Object.defineProperty);r.C.defineMode("graphql-variables",(e=>{const t=(0,l.o)({eatWhitespace:e=>e.eatSpace(),lexRules:i,parseRules:c,editorConfig:{tabSize:e.tabSize}});return{config:e,startState:t.startState,token:t.token,indent:n.i,electricInput:/^\s*[}\]]/,fold:"brace",closeBrackets:{pairs:'[]{}""',explode:"[]{}"}}}));const i={Punctuation:/^\[|]|\{|\}|:|,/,Number:/^-?(?:0|(?:[1-9][0-9]*))(?:\.[0-9]*)?(?:[eE][+-]?[0-9]+)?/,String:/^"(?:[^"\\]|\\(?:"|\/|\\|b|f|n|r|t|u[0-9a-fA-F]{4}))*"?/,Keyword:/^true|false|null/},c={Document:[(0,l.p)("{"),(0,l.l)("Variable",(0,l.b)((0,l.p)(","))),(0,l.p)("}")],Variable:[s("variable"),(0,l.p)(":"),"Value"],Value(e){switch(e.kind){case"Number":return"NumberValue";case"String":return"StringValue";case"Punctuation":switch(e.value){case"[":return"ListValue";case"{":return"ObjectValue"}return null;case"Keyword":switch(e.value){case"true":case"false":return"BooleanValue";case"null":return"NullValue"}return null}},NumberValue:[(0,l.t)("Number","number")],StringValue:[(0,l.t)("String","string")],BooleanValue:[(0,l.t)("Keyword","builtin")],NullValue:[(0,l.t)("Keyword","keyword")],ListValue:[(0,l.p)("["),(0,l.l)("Value",(0,l.b)((0,l.p)(","))),(0,l.p)("]")],ObjectValue:[(0,l.p)("{"),(0,l.l)("ObjectField",(0,l.b)((0,l.p)(","))),(0,l.p)("}")],ObjectField:[s("attribute"),(0,l.p)(":"),"Value"]};function s(e){return{style:e,match:e=>"String"===e.kind,update(e,t){e.name=t.value.slice(1,-1)}}}u(s,"name",{value:"namedKey",configurable:!0})}}]);