import xml.etree.ElementTree as et

with open("mmax-mini-plain.txt", "w") as txt:
    # opens the first three files
    for i in range(1,4):
        file_name = "mmax-all/Basedata/export_00{0}_words.xml".format(i)
        markable_file_name = "mmax-all/markables/export_00{0}_markable_level.xml".format(i)
        tree = et.parse(file_name)
        
        # creates a dictionary which looks like this:
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
        
        # writes file name for differentiation
        txt.write("Datei: {0}\n".format(file_name))
        # iterates over all words in words file
        for child in tree.getroot():
            id = child.get("id")
            start_end_dict = markable_dict.get(id)
            if start_end_dict:
                start = start_end_dict.get("start_of")
                end = start_end_dict.get("end_of")
            else:
                start, end = None, None
            # if the word is the start of a markable, adds start tag
            if start:
                for markable_id in start:
                     print "<{0}>".format(markable_id),
                     txt.write("<{0}>".format(markable_id))
            # writes word
            print "{0}".format(child.text),
            txt.write("{0}".format(child.text))
            # if the word is end of a markable, adds end tag
            if end:
                for markable_id in end:
                    print "</{0}>".format(markable_id),
                    txt.write("</{0}>".format(markable_id))
            print " ",
            txt.write(" ")
            # puts every sentence on a separate line
            if child.text in ("?", ".", "/"):
                print
                txt.write("\n")    
        print
        txt.write("\n\n")