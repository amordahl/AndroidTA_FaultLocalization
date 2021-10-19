grammar GeneralArray;

/*
 * My first grammar!
 * Got a lot of help from the following resources:
 *
 * https://stackoverflow.com/questions/56779101/how-do-i-parse-an-array-in-antlr
 */
 
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