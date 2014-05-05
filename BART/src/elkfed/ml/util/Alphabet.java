/*
 * Copyright 2007 EML Research
 * Copyright 2007 Project ELERFED
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package elkfed.ml.util;

import gnu.trove.map.hash.TObjectIntHashMap;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

/** A mapping between integers and objects where the mapping in each
 *  direction is efficient. The trick is given by (1) using an ArrayList
 *  for going from integers to objects and a map to go from objects to
 *  integers; (2) use the GNU trove libraries. Integers are assigned
 *  consecutively, starting at zero, as objects are added to the Alphabet.
 *  Objects can not be deleted from the Alphabet and thus the integers
 *  are never reused.
 *  <p>
 *  The most common use of an alphabet is as a dictionary of feature names.
 *  This class is simply a simplified version of the same Alphabet class
 *  given in the MALLET toolkit (just removed methods and added Java 5
 *  generics).
 */

public class Alphabet<T> implements Serializable
{
    private static final long serialVersionUID = 0xfeedfeeb1ef42L;

	/** The actual map */
    private TObjectIntHashMap map;
    
    /** The actual entries */
    private ArrayList<T> entries;
    private boolean _growing=true;
    
    /** The class of the entries */

    /** Creates a new instance of Alphabet */
    public Alphabet(int capacity)
    {
        this.map = new TObjectIntHashMap(capacity);
        this.entries = new ArrayList<T>(capacity);
    }

    public void stopGrowth() {_growing=false;}
    public void startGrowth() {_growing=true;}
    
    /** Creates a new instance of Alphabet */
    public Alphabet ()
    { this(16); }

    /** Return the index of a given object. Add the Object to this if not
     *  present */
    public int lookupIndex (T entry)
    {
        if (entry == null)
        { throw new IllegalArgumentException ("Can't lookup \"null\" in an Alphabet."); }

        if (map.containsKey(entry))
        { return map.get( entry ); }
        else if (_growing)
        {
            int toReturn = entries.size();
            map.put(entry, toReturn);
            entries.add(entry);
            return toReturn;
        } else {
            return -1;
        }
    }

    public T lookupObject(int index)
    { return entries.get(index); }

    public Object[] toArray()
    { return entries.toArray(); }


    public boolean contains (Object entry)
    { return map.contains (entry); }

    public int size ()
    { return entries.size(); }

    public void dump()
    {
        for (int i = 0; i < entries.size(); i++)
        { System.out.println (i + " => "+ entries.get (i)); }
    }

    /** Return String representation of all Alphabet entries,
     *  each separated by a newline. */
    @Override
    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < entries.size(); i++) {
                sb.append (entries.get(i).toString());
                sb.append ('\n');
        }
        return sb.toString();
    }
    
    // --------------------> SERIALIZATION <--------------------
    
    public void save(File saveFile)
    { 
        try
        {
            writeObject(new ObjectOutputStream(new FileOutputStream(saveFile)));
        }
        catch (IOException ioe) {
            throw new RuntimeException("Cannot save alphabet",ioe);
        }
    }
    
    private void writeObject(ObjectOutputStream out) throws IOException
    {
        out.writeInt(entries.size());
        for (int i = 0; i < entries.size(); i++)
        { out.writeObject(entries.get(i)); }
    }
    
    public void load(File loadFile)
    {
        try
        {
            readObject(new ObjectInputStream(new FileInputStream(loadFile)));
        }
        // TODO: do proper exception handling
        catch (IOException ioe) {
            throw new RuntimeException("Cannot load alphabet",ioe);
        }
        catch (ClassNotFoundException cnfe) {
            throw new RuntimeException("Cannot load alphabet",cnfe);
        }
    }
    
    private void readObject (ObjectInputStream in)
                            throws IOException, ClassNotFoundException
    {
        int size = in.readInt();
        this.map = new TObjectIntHashMap(size);
        this.entries = new ArrayList<T>(size);
        for (int i = 0; i < size; i++)
        {
            T o =(T) in.readObject();
            this.map.put(o, i);
            this.entries.add(o);
        }
    }
}
