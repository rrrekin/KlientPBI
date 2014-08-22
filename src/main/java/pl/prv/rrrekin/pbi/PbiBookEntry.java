/*
 * Copyright 2014 Michał Rudewicz.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pl.prv.rrrekin.pbi;

/**
 * <p>PbiBookEntry class.</p>
 *
 * @author Michał Rudewicz
 * @version $Id: $Id
 */
public class PbiBookEntry {
    private String title;
    private String author;
    private int id;

    /**
     * <p>Constructor for PbiBookEntry.</p>
     *
     * @param title a {@link java.lang.String} object.
     * @param author a {@link java.lang.String} object.
     * @param id a {@link java.lang.String} object.
     * @throws java.lang.NumberFormatException if any.
     */
    public PbiBookEntry(String title, String author, String id) throws NumberFormatException{
        this.title = title;
        this.author = author;
        this.id = Integer.parseInt(id);
    }

    
    /**
     * <p>Getter for the field <code>title</code>.</p>
     *
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * <p>Setter for the field <code>title</code>.</p>
     *
     * @param title the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * <p>Getter for the field <code>author</code>.</p>
     *
     * @return the author
     */
    public String getAuthor() {
        return author;
    }

    /**
     * <p>Setter for the field <code>author</code>.</p>
     *
     * @param author the author to set
     */
    public void setAuthor(String author) {
        this.author = author;
    }

    /**
     * <p>Getter for the field <code>id</code>.</p>
     *
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * <p>Setter for the field <code>id</code>.</p>
     *
     * @param id the id to set
     */
    public void setId(int id) {
        this.id = id;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('"').append(title).append("\", ").append(author).append(" (").append(id).append(")");
        
        return sb.toString();
    }
    
    
    
}
