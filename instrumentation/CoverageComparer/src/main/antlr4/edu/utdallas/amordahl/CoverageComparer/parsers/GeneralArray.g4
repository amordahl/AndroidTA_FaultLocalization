grammar GeneralArray;
 
/*
 * Parser Rules
 */
 
array
	: LEFT_BRACKET element* RIGHT_BRACKET 
	;
	
element
	: (string | array) delimiter?
	;
	
delimiter
	: DELIMITER SPACE?
	;

string
	: (ALLOWED_CHARACTER | SPACE)+
	;
	
/*
 * Lexer Rules
 */
 
LEFT_BRACKET
 	: '['
 	;
 	
RIGHT_BRACKET
 	: ']'
 	;
 	
DELIMITER
 	: ','
 	;

SPACE
	: ' '
	;
	
IGNORABLE_WS
	:   [\t\r\n]+ -> skip
    	;

ALLOWED_CHARACTER
 	: .
	;


