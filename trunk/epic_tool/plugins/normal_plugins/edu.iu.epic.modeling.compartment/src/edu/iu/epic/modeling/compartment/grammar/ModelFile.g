grammar ModelFile;

options {
	output = template; // For list labels (like (ids+=compartmentID)+)
}

@parser::header {
package edu.iu.epic.modeling.compartment.grammar.parsing;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import edu.iu.epic.modeling.compartment.model.Compartment;
import edu.iu.epic.modeling.compartment.model.Model;
import edu.iu.epic.modeling.compartment.model.exceptions.ModelModificationException;
}

@lexer::header {
package edu.iu.epic.modeling.compartment.grammar.parsing;
}

@parser::members {
// The model that this parser will produce.
private Model model = new Model();
public Model getModel() {
	return model;
}

public static class UncheckedParsingException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	
	public UncheckedParsingException(Throwable cause) {
		super(cause);
	}
	
	public UncheckedParsingException(String message) {
		super(message);
	}
}


public static ModelFileParser createParserOn(ANTLRStringStream stream) {
	ModelFileLexer lex = new ModelFileLexer(stream);		
	CommonTokenStream tokens = new CommonTokenStream(lex);
   	return new ModelFileParser(tokens);
}

public static ModelFileParser createParserOn(String input) {
   	return createParserOn(new ANTLRStringStream(input));
}

public static ModelFileParser createParserOn(File file) throws IOException {
	return createParserOn(new ANTLRFileStream(file.getCanonicalPath()));
}
}


// Glob multiple spaces or tabs into one ignored token.
WHITESPACE
	: ('\t' | ' ')+ {$channel = HIDDEN;}
	;


// Global
modelFile
	: line EOF
	| (NEWLINE | (line NEWLINE))+ EOF
	;
// TODO This probably needs to be broken down into sections so that the final compartmentDeclaration must come before the first transitionRule.  Depends upon how we code the Object model.
line
	: COMMENT
	| (ID '=' .*)=> parameterAssignment
	| compartmentDeclaration
	| (ID ('--' | '->') .*)=> transitionRule
	;
NEWLINE
	: ('\r'? '\n')=> '\r'? '\n'
	| '\r'
	;
COMMENT
	: '#' (~('\n'|'\r'))* {$channel = HIDDEN;}
	;


// Parameters
parameterAssignment
	: parameterID '=' parameterValue[new HashSet()]	{ model.setParameterDefinition($parameterID.text, $parameterValue.text); }
	;
	catch[ModelModificationException e] { throw new UncheckedParsingException(e); }
parameterID
	: ID
	;
parameterValueValidator[Set referencedParameters]
	: parameterValue[referencedParameters] EOF
	;
parameterValue[Set referencedParameters]
	: arithmeticParameterExpression[referencedParameters]
	;	
arithmeticParameterExpression[Set referencedParameters]
	: arithmeticParameterTerm[referencedParameters] (('+' | '-') arithmeticParameterTerm[referencedParameters])*
	;
arithmeticParameterTerm[Set referencedParameters]
	: arithmeticParameterFactor[referencedParameters] (('*' | '/') arithmeticParameterFactor[referencedParameters])*
	;
arithmeticParameterFactor[Set referencedParameters]
	: parameterID { referencedParameters.add($parameterID.text); }
	| number
	;


// Compartment declarations
compartmentDeclaration
	: susceptibleCompartmentDeclaration
	| infectedCompartmentDeclaration
	| latentCompartmentDeclaration
	| recoveredCompartmentDeclaration
	;
susceptibleCompartmentDeclaration
	: 'susceptible' compartmentID	{ model.addCompartment($compartmentID.text, Compartment.Type.SUSCEPTIBLE); }
	;
	catch[ModelModificationException e] { throw new UncheckedParsingException(e); }
infectedCompartmentDeclaration
	: 'infection' compartmentID	{ model.addCompartment($compartmentID.text, Compartment.Type.INFECTED); }
	;
	catch[ModelModificationException e] { throw new UncheckedParsingException(e); }
latentCompartmentDeclaration
	: 'latent' compartmentID	{ model.addCompartment($compartmentID.text, Compartment.Type.LATENT); }
	;
	catch[ModelModificationException e] { throw new UncheckedParsingException(e); }
recoveredCompartmentDeclaration
	: 'recovered' compartmentID	{ model.addCompartment($compartmentID.text, Compartment.Type.RECOVERED); }
	;
	catch[ModelModificationException e] { throw new UncheckedParsingException(e); }


// Transitions
transitionRate
	: parameterValue[new HashSet()]
	;
transitionRule
	@init { boolean isSecondary = false; }
	: spontaneousTransitionRelation transitionRate ('secondary' { isSecondary = true; })?
		{
		model.addSpontaneousTransition(
			model.getCompartment($spontaneousTransitionRelation.source),
			model.getCompartment($spontaneousTransitionRelation.target),
			$transitionRate.text,
			isSecondary);
		}
	| interactionTransitionRelation transitionRate ('secondary' { isSecondary = true; })?
		{
		model.addInteractionTransition(
			model.getCompartment($interactionTransitionRelation.source),
			model.getCompartment($interactionTransitionRelation.interactor),
			model.getCompartment($interactionTransitionRelation.target),
			$transitionRate.text,
			isSecondary);
		}
	;
	catch[ModelModificationException e] { throw new UncheckedParsingException(e); }
spontaneousTransitionRelation returns [String source, String target]
	: s=compartmentID '->' t=compartmentID
		{
		$source = $s.text;
		$target = $t.text;
		}
	;
compartmentID
	: ID
	;
compartmentIDValidator
	: compartmentID EOF
	;
interactionTransitionRelation returns [String source, String interactor, String target]
	: s=compartmentID '--' i=compartmentID '=' t=compartmentID
		{
		$source = $s.text;
		$interactor = $i.text;
		$target = $t.text;
		}
	;


// Useful constants
fragment NUMERIC
	: '0'..'9'
	;
/*
fragment ALPHABETIC
	: 'a'..'z'
	| 'A'..'Z'
	;
fragment ALPHABETIC_
	: ALPHABETIC
	| '_'
	;
fragment ALPHANUMERIC
	: ALPHABETIC
	| NUMERIC
	;
fragment ALPHANUMERIC_
	: ALPHANUMERIC
	| '_'
	;

ID
	: ALPHABETIC_ (ALPHANUMERIC_)*
	;
*/
ID 
	: ('a'..'z'|'A'..'Z'|'_') ('a'..'z'|'A'..'Z'|'0'..'9'|'_')*
   	;


number
	: ('-')? UNSIGNED_NUMBER
	;
UNSIGNED_NUMBER
	: (NUMERIC)+ ('.' (NUMERIC)+)?
	;
