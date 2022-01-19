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
 * Represents an AQL flow
 */
public class Flow {

    private Statement sink;
    private Statement source;
    private String id;
    private String generatingConfig;

    public boolean getClassification() {
        return classification;
    }

    public void setClassification(boolean classification) {
        this.classification = classification;
    }

    private boolean classification;
    public String getApk() {
        return apk;
    }

    public void setApk(String apk) {
        this.apk = apk;
    }

    private String apk;

    /**
     * Initializes the flow with no data.
     */
    public Flow() {
        this.sink = new Statement();
        this.source = new Statement();
    }

    public Statement getSource() { return this.source; }
    public Statement getSink() {return this.sink; }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getGeneratingConfig() {
        return generatingConfig;
    }

    public void setGeneratingConfig(String generatingConfig) {
        this.generatingConfig = generatingConfig;
    }

    enum Tool {
        FLOWDROID,
        AMANDROID,
        DROIDSAFE
    }

    /**
     * Returns the tool that generated the flow. Does this based off of the ID.
     * @return An element of the Tool enumeration, or null if the tool could not be determined.
     */
    public Tool getGeneratingTool() {
        // Id may be null. In that case, we return null.
        if (this.id == null) {
            return null;
        } else {
            // check against the first two characters of the tool
            switch (this.id.substring(0, 2).toLowerCase()) {
                case "fd":
                    return Tool.FLOWDROID;
                case "ds":
                    return Tool.DROIDSAFE;
                case "am":
                    return Tool.AMANDROID;
                default:
                    throw new RuntimeException(String.format("Could not determine which tool ID %s came from.", this.id));
            }
        }
    }
    @Override
    public boolean equals(Object o){
        if(!(o instanceof Flow))
            return false;
        if(o==this)
            return true;
        Flow f = (Flow) o;
        return (f.getSink().equals(this.getSink()))&&(f.getSource().equals(this.getSource()));
    }
}

