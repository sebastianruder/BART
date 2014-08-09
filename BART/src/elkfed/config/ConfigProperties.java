/*
 * ConfigProperties.java
 *
 * Created on July 10, 2007, 5:26 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package elkfed.config;

import elkfed.coref.mentions.AbstractMentionFactory;
import elkfed.coref.mentions.DefaultMentionFactory;
import elkfed.coref.mentions.MentionFactory;
import elkfed.lang.EnglishLanguagePlugin;
import elkfed.lang.EnglishLinguisticConstants;
import elkfed.lang.GermanLanguagePlugin;
import elkfed.lang.GermanLinguisticConstants;
import elkfed.lang.ItalianLanguagePlugin;
import elkfed.lang.LanguagePlugin;
import elkfed.lang.LinguisticConstants;
import elkfed.mmax.pipeline.DefaultPipeline;
import elkfed.mmax.pipeline.ParserPipeline;
import elkfed.mmax.pipeline.Pipeline;
import elkfed.mmax.pipeline.Parser;
import elkfed.mmax.pipeline.CharniakParser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/** Loads configuration properties from a config file.
 *
 * @author ajern
 */
public class ConfigProperties {

    /** IMPLEMENTATION DETAIL: the singleton instance */
    private static ConfigProperties singleton;

    /** Getter for instance */
    public static synchronized ConfigProperties getInstance() {
        if (singleton == null) {
            singleton = new ConfigProperties();
        }
        return singleton;
    }
    public static final String MUC6_ID = "MUC6";
    public static final String TRAINING_DATA = "trainData";
    public static final String TRAINING_DATA_ID = "trainDataId";
    public static final String TRAINING_DATA_SINK = "trainDataSink";
    public static final String TEST_DATA = "testData";
    public static final String TEST_DATA_ID = "testDataId";
    public static final String DEFAULT_DATA_ID = "UNK";
    public static final String PIPELINE = "pipeline";
    public static final String YAMCHA_EXE = "yamchaExe";
    public static final String ASSERT_DIR = "assertDir";
    public static final String SVMLEARN_EXE = "svmlearn";
    public static final String SVMCLASSIFY_EXE = "svmclassify";
    public static final String CHARNIAK_DIR = "charniakDir";
    public static final String NAMES_MALE_DB = "maleNamesDB";
    public static final String NAMES_FEMALE_DB = "femaleNamesDB";
    public static final String NAMES_BERGSMA_DB = "bergsmaGenderDB";
    public static final String MODEL_DIR = "modelDir";
    public static final String MODEL_NAME = "modelName";
    public static final String DNEW_MODEL_NAME = "dnewmodelName";
    public static final String MSN_APP_ID = "msn_app_id";
    public static final String MENTION_FACTORY = "mentionFactory";
    public static final String FILTER_PRENOMINALS = "filterPrenomChains";
    public static final String BALANCE_KEY_AND_RESPONSE = "balanceKeyAndResponse";
    public static final String ADD_SINGLETONS_TO_KEY = "addSingletonsToKey";
    public static final String DEFAULT_SYSTEM = "default_system";
    public static final String DEFAULT_PARSER = "parser";
    public static final String PERFECT_MENTIONS = "perfectMentions";
    public static final String MMAX2MUC = "mmax2muc";
    public static final String MARKABLE_LEVEL_CONF = "markableLevelConfig";
    public static final String TAXONOMY_PAIRS_FULL = "taxonomyPairFull";
    public static final String TAXONOMY_PAIRS_HEADS = "taxonomyPairHeads";
    public static final String DBGPRINT = "verboseoutput";
    public static final String WNHOME = "wnhome";
    public static final String POSSOUT = "fullpossessives";
    public static final String MARKCLEANUP = "domarkablecleanup";
    public static final String SGOUT = "singletons";
    public static final String DNEWTHR = "dnewthr";
    public static final String DOLDTHR = "doldthr";
    private static final String PROP_FILE = "config/config.properties";
    private final Properties prop;
    private Pipeline pipeline;
    private Parser parser;
    private MentionFactory factory;
    private File rootDir;
    private LanguagePlugin langPlugin;
    private LinguisticConstants constants;
    private static final String RUN_PIPELINE = "runPipeline";

    /** gets the machine's host name, or null */
    public String getHostname() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException ex) {
            return null;
        }
    }

    /** Creates a new instance of ConfigProperties */
    private ConfigProperties() {
        // loads the properties
        this.prop = new Properties();
        try {
            String root = System.getProperty("elkfed.rootDir", ".");
            String hostname = getHostname();
            rootDir = new File(root);
            FileInputStream in = null;
            if (hostname != null) {
                try {
                    in = new FileInputStream(
                            new File(rootDir, PROP_FILE + "." + hostname));
                } catch (FileNotFoundException ex) {
                    // well, bad luck then.
                }
            }
            if (in == null) {
                in = new FileInputStream(
                        new File(rootDir, PROP_FILE));
            }
            prop.load(in);
            in.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public LanguagePlugin getLanguagePlugin() {
        if (langPlugin == null) {
            String lang = getCorpusProperty("language", "english").toLowerCase();
            if (lang.startsWith("eng")) {
                langPlugin = new EnglishLanguagePlugin();
            } else if (lang.startsWith("ita")) {
                langPlugin = new ItalianLanguagePlugin();
            } else if (lang.startsWith("deu")) {
                langPlugin = new GermanLanguagePlugin();
            } else {
                throw new UnsupportedOperationException("No LanguagePlugin for " + lang);
            }
        }
        return langPlugin;
    }
    
    public LinguisticConstants getLinguisticConstants() {
    	if (constants == null) {
            String lang = getCorpusProperty("language", "english").toLowerCase();
            if (lang.startsWith("eng")) {
                constants = new EnglishLinguisticConstants();
            } else if (lang.startsWith("ita")) {
            	// no ItalianLinguisticConstants yet
            } else if (lang.startsWith("deu")) {
                constants = new GermanLinguisticConstants();
            } else {
                throw new UnsupportedOperationException("No LanguagePlugin for " + lang);
            }
        }
        return constants;
    	
    }

    /** Retreive a specified property */
    public String getProperty(String p)
    { return prop.getProperty(p); }


    public String getCorpusProperty(String p, String def)
    {
        String result=prop.getProperty(p,def);
        String corpus_id=System.getProperty("elkfed.corpus");
        if (corpus_id!=null)
        {
            result=prop.getProperty(p+"."+corpus_id,result);
        }
        return result;
    }

    /** Gets the training data dir */
    public File getTrainingData()
    { return new File(getCorpusProperty(TRAINING_DATA, "./sample/ACE-02")); }

    /** Gets the training data id */
    public String getTrainingDataId()
    { return getCorpusProperty(TRAINING_DATA_ID,
              System.getProperty("elkfed.corpus",DEFAULT_DATA_ID)); }

    /** Gets the training data id */
    public String getTrainingDataSink()
    { return prop.getProperty(TRAINING_DATA_SINK, "train.data"); }

    /** Gets the test data dir */
    public File getTestData()
    { return new File(getCorpusProperty(TEST_DATA, "./sample/ACE-02")); }

    /** Gets the test data id */
    public String getTestDataId()
    { return getCorpusProperty(TEST_DATA_ID,
              System.getProperty("elkfed.corpus",DEFAULT_DATA_ID)); }

    public Set<String> getGoldLevels() {
        String lvlS=getCorpusProperty("goldLevels",
                "section,lex,coref");
        Set<String> retval=new HashSet<String>();
        for (String s: lvlS.split(","))
        {
            retval.add(s);
        }
        return retval;
    }

    public File getRoot() {
        return rootDir;
    }

    /** Gets the model dir */
    public File getModelDir()
    {
        String modelDir=prop.getProperty(MODEL_DIR, "./models/coref");
        File modelFile=new File(modelDir);
        if (modelFile.isAbsolute()) {
            return modelFile;
        } else {
            return new File(getRoot(),modelDir);
        }
    }

    /** Gets the model dir */
    public String getModelName()
    { return prop.getProperty(MODEL_NAME, "model.model"); }

    /** Gets the dnew model dir */
    public String getDnewModelName()
    { return prop.getProperty(DNEW_MODEL_NAME, "dnew.model"); }


    /* open <i>fileName</i> as a UTF-8 file and return
     * a BufferedReader object that reads from it.
     */
    public BufferedReader openNamesFile(String fileName) {
        File in_file = new File(getRoot(), fileName);
        BufferedReader result;
        try {
            result = new BufferedReader(new InputStreamReader(new FileInputStream(in_file), "UTF-8"));
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException("UTF-8 encoding not supported!?", ex);
        } catch (FileNotFoundException ex) {
            throw new RuntimeException("Cannot open " + fileName, ex);
        }
        return result;
    }
    public BufferedReader openNames(String basename) {
        return openNamesFile(prop.getProperty(basename,"./names/"+basename+".txt"));
    }

    /** Gets the male name DB */
    public BufferedReader openMaleNamesDB()
    { return openNames("person_male"); }

    /** Gets the female name DB */
    public BufferedReader openFemaleNamesDB()
    { return openNames("person_female"); }

    public BufferedReader openBergsmaGenderDB()
    { return openNames("gender_db"); }

    public BufferedReader openStopList()
    { return openNames("stopwords"); }

    public BufferedReader openAdjMap()
    { return openNames("adj_map"); }

    public boolean getRunPipeline()
    { return Boolean.parseBoolean(prop.getProperty(RUN_PIPELINE, "true")); }

    public File getBergsmaGenderDB()
    { return new File(prop.getProperty(NAMES_BERGSMA_DB, "./names/gender.txt")); }

    public File getStopList()
    { return new File(prop.getProperty("stopwordDB", "./names/englishST.txt")); }

    public File getAdjMap()
    { return new File(prop.getProperty("AdjMap", "./names/adj_map.txt")); }

    public File getStrudelDB()
    { return new File(prop.getProperty("StrudelDB", "./wordpairs/strudelPairsDB")); }

    /** Gets the parser */
    public Parser getParser()
    {
        if (this.parser == null)
        {
            // sets the pipeline once and for all
            try {
                this.parser =
                        (Parser) Class.forName(prop.getProperty(DEFAULT_PARSER, "elkfed.mmax.pipeline.CharniakParser")).newInstance();
            }
            catch (Exception e)
            { this.parser = new CharniakParser(); }
        }
        return parser;
    }

    /** Gets the pipeline */
    public Pipeline getPipeline()
    {
        if (this.pipeline == null)
        {
            // sets the pipeline once and for all
            try {
                this.pipeline =
                        (Pipeline) Class.forName(getCorpusProperty(PIPELINE, "elkfed.mmax.pipeline.ParserPipeline")).newInstance();
//                        (Pipeline) Class.forName(getCorpusProperty(PIPELINE, "elkfed.mmax.pipeline.DefaultPipeline")).newInstance();
            }
            catch (Exception e)
            { this.pipeline = new ParserPipeline(); }
//            { this.pipeline = new DefaultPipeline(); }
        }
        return pipeline;
    }

    /** Gets the mention factory */
    public MentionFactory getMentionFactory()
    {
        if (this.factory == null)
        {
            Boolean perfect =
                    Boolean.parseBoolean(
                    prop.getProperty(PERFECT_MENTIONS, "false")
                );

            // sets the mention factory once and for all
            try {
                this.factory =
                        (MentionFactory) Class.forName(getCorpusProperty(MENTION_FACTORY, "elkfed.coref.mentions.FullDocMentionFactory")).newInstance();
            }
            catch (Exception e)
            {
                System.err.println("Using DefaultMentionFactory since an exception occurred:");
                e.printStackTrace();
                this.factory = new DefaultMentionFactory();
            }

            if (this.factory instanceof AbstractMentionFactory)
            {
                if(perfect) System.err.println("Setting TRUE MENTIONS");
                ((AbstractMentionFactory) this.factory).setPerfectBoundaries(perfect);
            }
        }
        return factory;
    }

    /** Gets the path to the executable for Yamcha */
    public String getYamcha()
    { return prop.getProperty(YAMCHA_EXE, "/usr/local/bin/yamcha"); }

    /** Gets whether to filter out chains of prenominals only */
    public boolean getFilterPrenominals()
    { return Boolean.parseBoolean(getCorpusProperty(FILTER_PRENOMINALS, "false")); }

    /** Gets whether to filter out sets with just one mention in it that are singleton in key and response*/
    public boolean getBalanceKeyAndResponse()
    { return Boolean.parseBoolean(getCorpusProperty(BALANCE_KEY_AND_RESPONSE, "false")); }

    /** Gets whether to add all singletons to response that are in the key*/
    public boolean getAddSingletons()
    { return Boolean.parseBoolean(getCorpusProperty(ADD_SINGLETONS_TO_KEY, "false")); }

    public boolean getDbgPrint()
    { return Boolean.parseBoolean(getCorpusProperty(DBGPRINT, "false")); }

    public boolean getFullPossessives()
    { return Boolean.parseBoolean(getCorpusProperty(POSSOUT, "false")); }

    public boolean getMarkCleanup()
    { return Boolean.parseBoolean(getCorpusProperty(MARKCLEANUP, "true")); }

    public boolean getOutputSingletons()
    { return Boolean.parseBoolean(getCorpusProperty(SGOUT, "false")); }

    public double getDnewThr()
//    { return Double.parseDouble(prop.getProperty(DNEWTHR,"0")); }
    { return Double.parseDouble(System.getProperty("elkfed.dnewthr","0")); }

    public double getDoldThr()
    { return Double.parseDouble(System.getProperty("elkfed.doldthr","0")); }
//    { return Double.parseDouble(prop.getProperty(DOLDTHR, "0")); }

    public String getCharniakDir()
    { return prop.getProperty(CHARNIAK_DIR, "/usr/local/lib/reranking-parser"); }

    public String getSVMLightLearn()
    { return prop.getProperty(SVMLEARN_EXE, "/usr/local/bin/svm_learn"); }

    public String getSVMLightClassify()
    { return prop.getProperty(SVMCLASSIFY_EXE, "/usr/local/bin/svm_classify"); }

    public String getDefaultSystem()
    { return prop.getProperty(DEFAULT_SYSTEM, "idc0_system"); }

    public String getMMAX2MUC()
    { return new File(prop.getProperty(MMAX2MUC, "config/MMAX2MUC.xsl")).getAbsolutePath(); }

    public String getSvmLibrary()
    {
        return prop.getProperty("svmlight_tk_so",
                new File("./scripts/svmlight_tk.so").getAbsolutePath());
    }

    public File getMarkableLevels()
    { return new File(prop.getProperty(MARKABLE_LEVEL_CONF, "config/markables.xml")); }

    public String getNGramDir()
    { return prop.getProperty("ngram_dir", ""); }

    public String getMSNAppID()
    { return prop.getProperty(MSN_APP_ID,"-- please add MSN APP ID to your config --"); }

    public String getMorphgExe()
    { return prop.getProperty("morphg","morphg"); }

    public File getTaxonomyPairsFull()
    { return new File(prop.getProperty(TAXONOMY_PAIRS_FULL,"./models/wikipedia/full.txt")); }

    public File getTaxonomyPairsHeads()
    { return new File(prop.getProperty(TAXONOMY_PAIRS_HEADS,"./models/wikipedia/heads.txt")); }

    public String getWNHome()
    { return prop.getProperty(WNHOME,"./wordnet"); }

//public String getAssertDir()
//{ return prop.getProperty(ASSERT_DIR,"./assert/"); }

    public String getAssertDir()
    { return prop.getProperty(ASSERT_DIR, "/usr/local/bin/assert"); }

    public Connection getWikiDBConnection()
    {
        try {
            Class.forName(prop.getProperty("wikiDB_driver",
                    "com.mysql.jdbc.Driver"));
            String userName=prop.getProperty("wikiDB_user",
                    "root");
            String password=prop.getProperty("wikiDB_password",
                    "root");
            return
            DriverManager.getConnection(prop.getProperty("wikiDB_dburl",
                    "jdbc:mysql://ndc04:3306/wikidb?useOldUTF8Behavior=true&useUnicode=true&characterEncoding=UTF-8"),
                    userName,password);
        } catch (SQLException ex) {
            ex.printStackTrace();
            throw new RuntimeException("Cannot connect to database",ex);
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException("Cannot load DB driver",ex);
        }
    }
}
