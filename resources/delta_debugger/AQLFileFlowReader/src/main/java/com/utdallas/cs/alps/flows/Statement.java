/*
  This file is part of Tamis.

  Tamis is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  Tamis is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with Tamis.  If not, see <https://www.gnu.org/licenses/>.
 */

/*
  Created by Austin Mordahl, June 2020.
 */

package com.utdallas.cs.alps.flows;

/**
 * Describes a Statement from an AQL-report, including the
 * statement itself, the classname, and the method name.
 */
public class Statement {
    private String statement;
    private String statementFull;
    private String classname;
    private String method;

    public Statement() {
        statement = null;
        classname = null;
        method = null;
        statementFull=null;
    }

    public String getStatement() {
        return statement;
    }

    /**
     * Sets the actual statement from the flow report.
     *
     * @param statement The statement (typically the statementgeneric field from AQL).
     */
    public void setStatement(String statement) {
        this.statement = statement;
    }

    public String getStatementFull() {
        return statementFull;
    }

    /**
     * Sets the actual statement from the flow report.
     *
     * @param statementFull The statement (typically the statementgeneric field from AQL).
     */
    public void setStatementFull(String statementFull) {
        this.statementFull = statementFull;
    }

    public String getClassname() {
        return classname;
    }

    public void setClassname(String classname) {
        this.classname = classname;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    /**
     * We consider two statements to be equal if their statement,
     * classname, and method are the same.
     *
     * @param obj the other statement.
     * @return true if the statments are equal, false otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Statement) {
            Statement other = (Statement) obj;
            return this.statement.equals(other.statement) &&
                    this.method.equals(other.method) &&
                    this.classname.equals(other.classname)/* &&
                    this.statementFull.replaceAll("\\$r\\d+","r").equals(other.statementFull.replaceAll("\\$r\\d+","r"))*/;
        } else {
            return false;
        }
    }
}
