package elkfed.mmax.converters;

import java.io.*;
import java.util.*;

public class Mention {

    // MUC information:
    public String corefId       = null;
    public String corefRefId    = null;
    // ACE and MUC information:
    public String entityType    = null;
    // ACE information:
    //   Entity-level info:
    public String entityId      = null;
    public String entitySubType = null;
    public String entityClass   = null;
    //   Mention-level info:
    public String text          = null;
    public String startOffset   = null;
    public String endOffset     = null;
    public String mentionId     = null;
    public String mentionType   = null;
    public String mentionRole   = null;
    public String mentionMetonymy = null;
    public String mentionLDCType  = null;
    public String mentionLDCAtr   = null;
    // ACE-CDC information:
    public String entityCDCId   = null;
    // Used by this program:
    public Mention nextMention  = null; // Defines chain of mentions for a particular entity
    public int cycleMarker      = 0;    // Used to detect cycles in COREF REFID's.

    public String toString() {
        return text;
    }

}
