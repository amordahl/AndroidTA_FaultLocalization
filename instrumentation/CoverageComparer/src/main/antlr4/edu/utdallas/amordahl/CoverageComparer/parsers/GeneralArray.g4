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
	: DELIMITER ' '?
	;

string
	: ALLOWED_CHARACTER+
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
 	
 ALLOWED_CHARACTER
 	: ~ [\r\n ,]
 	;