import xml.etree.ElementTree as et

with open("mmax-mini-plain.txt", "w") as txt:
    # opens the first three files
    for i in range(1,4):
        file_name = "mmax-mini/Basedata/export_00{0}_words.xml".format(i)
        markable_file_name = "mmax-mini/markables/export_00{0}_markable_level.xml".format(i)
        coref_file_name = "mmax-mini/markables/export_00{0}_coref_level.xml".format(i)
        tree = et.parse(file_name)
        
        # creates a markable dictionary which looks like this:
        # markable_dict = {"word_2": {"start_of": ["markable_13"]}, "word_3": {"end_of": ["markable_13"]}...}
        # as markables can be embedded
        markables = et.parse(markable_file_name).getroot()
        spans = [(markable.get("id").split("_")[1], markable.get("span")) for markable in markables]
        markable_dict = {}
        for markable_id, span in spans:
            if ".." in span:
                start = span.split("..")[0]
                end = span.split("..")[1]
            else:
                start, end = span, span
            markable_dict.setdefault(start, {})
            markable_dict.setdefault(end, {})
            markable_dict[start].setdefault("start_of", [])
            markable_dict[end].setdefault("end_of", [])
            markable_dict[start]["start_of"].append(markable_id)
            markable_dict[end]["end_of"].append(markable_id)
        
        # creates a coref dictionary which looks like this
        # coref_dict = {"word_32": {"start_of": ["1:502"]}, "word_34": {"end_of": ["1:502"]}...}
        corefs = et.parse(coref_file_name).getroot()
        coref_dict = {}
        spans = [(coref.get("coref_set"), coref.get("span")) for coref in corefs]
        for coref_set, span in spans:
            if ".." in span:
                start = span.split("..")[0]
                end = span.split("..")[1]
            else:
                start, end = span, span
            coref_dict.setdefault(start, {})
            coref_dict.setdefault(end, {})
            coref_dict[start].setdefault("start_of", [])
            coref_dict[end].setdefault("end_of", [])
            coref_dict[start]["start_of"].append(coref_set)
            coref_dict[end]["end_of"].append(coref_set)
            
        # writes file name for differentiation
        txt.write("Datei: {0}\n".format(file_name))
        # iterates over all words in words file
        for child in tree.getroot():
            id = child.get("id")
            # sets coref start and end if word is start or end of a coref
            coref_start_end_dict = coref_dict.get(id)
            if coref_start_end_dict:
                coref_start = coref_start_end_dict.get("start_of")
                coref_end = coref_start_end_dict.get("end_of")
            else:
                coref_start, coref_end = None, None
            # sets markable start and end if word is start or end of a markable  
            markable_start_end_dict = markable_dict.get(id)
            if markable_start_end_dict:
                markable_start = markable_start_end_dict.get("start_of")
                markable_end = markable_start_end_dict.get("end_of")
            else:
                markable_start, markable_end = None, None
            # if the word is the start of a coref, adds start tag
            if coref_start:
                for coref_set in coref_start:
                    print "<{0}>".format(coref_set),
                    txt.write("<{0}>".format(coref_set))
            # if the word is the start of a markable, adds start tag
            if markable_start:
                for markable_id in markable_start:
                     print "<{0}>".format(markable_id),
                     txt.write("<{0}>".format(markable_id))
            # writes word
            print "{0}".format(child.text),
            txt.write("{0}".format(child.text))
            # if the word is the end of a markable, adds end tag
            if markable_end:
                for markable_id in markable_end:
                    print "</{0}>".format(markable_id),
                    txt.write("</{0}>".format(markable_id))
            # if the word is the end of a coref, adds end tag
            if coref_end:
                for coref_set in coref_end:
                    print "</{0}>".format(coref_set),
                    txt.write("</{0}>".format(coref_set))
            print " ",
            txt.write(" ")
            # puts every sentence on a separate line
            if child.text in ("?", ".", "/"):
                print
                txt.write("\n")    
        print
        txt.write("\n\n")