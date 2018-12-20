package coreNLP.ParserDemo;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.SentenceUtils;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreebankLanguagePack;
import edu.stanford.nlp.trees.TypedDependency;

/**
 * Samples for using Stanford CoreNLP Parsers
 * 
 * @author Chirayu Desai
 *
 */
public class MyParser {
	public static void main(String[] args) {
		// Load the grammar for English Parser
		String grammar = args.length > 0 ? args[0]
				: "edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz";
		// Options
		String[] options = { "-maxLength", "50", "-retainTmpSubcategories", "-outputFormat",
				"wordsAndTags,penn,typedDependencies" };
		LexicalizedParser lp = LexicalizedParser.loadModel(grammar, options);
		TreebankLanguagePack tlp = lp.getOp().langpack();
		GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();

		String paragraph = "My 1st sentence. “Does it work for questions?” My third sentence.";

		String paragraph2 = paragraph;
		Reader reader = new StringReader(paragraph2);
		DocumentPreprocessor dp = new DocumentPreprocessor(reader);
		List<String> sentenceList = new ArrayList<>();

		for (List<HasWord> sentence : dp) {
			// SentenceUtils not Sentence
			String sentenceString = SentenceUtils.listToString(sentence);
			sentenceList.add(sentenceString.replaceAll("\\p{P}", "").replaceAll("`` ", ""));
		}
		int i = 1;
		System.out.println(sentenceList.size());
		MyParser app = new MyParser();

		List<String> sentenceFiltered = new ArrayList<>();
		for (String string : sentenceList) {
			int v = string.split("\\s+").length;
			if (0 < v && v <= 30) {
				sentenceFiltered.add(string);
			}
		}

		List<Object> results = sentenceFiltered.parallelStream()
				.map(s -> app.generateStanfordParserResult(s, lp, gsf))
				.collect(Collectors.toList());
		for (Object object : results) {
			ResultWrapper rw = (ResultWrapper) object;
			System.out.println(rw.toString());
			i++;
		}
		System.out.println("Number of sentences parsed : " + results.size());
	}

	/***
	 * Helper method to read a file into a string
	 * 
	 * @param path
	 *            the location of the file
	 * @return the String with contents of the file
	 */
	public static String readFile(String path) {
		String retVal = "";
		try (BufferedReader br = new BufferedReader(new FileReader(path))) {
			StringBuilder sb = new StringBuilder();
			String line = br.readLine();

			while (line != null) {
				sb.append(line);
				sb.append(System.lineSeparator());
				line = br.readLine();
			}
			String everything = sb.toString();
			retVal += everything;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return retVal;
	}

	/**
	 * Helper method to parse a sentence and generate a formatted output
	 * 
	 * @param sentence
	 *            the String that needs to be parsed
	 * @param lp
	 *            the Parser to be used
	 * @param gsf
	 *            the GrammaticalStructureFactory for this parser
	 */
	public static void generateStanfordParserOutput(String sentence, LexicalizedParser lp,
			GrammaticalStructureFactory gsf) {
		System.out.println(sentence.length());

		System.out.println("File : " + sentence);

		Tree parse = lp.parse(sentence);

		System.out.println("Word And Tags : ");
		printList(parse.taggedYield());
		System.out.println();
		System.out.println();

		System.out.println("Penn Parse : ");
		parse.pennPrint();
		System.out.println();
		System.out.println();

		System.out.println("Typed Dependencies : ");
		GrammaticalStructure grammaticalStructure = gsf.newGrammaticalStructure(parse);
		List<TypedDependency> typedDependencies = grammaticalStructure
				.typedDependenciesCCprocessed();
		printList(typedDependencies);
		System.out.println();
		System.out.println();

		System.out.println();

	}

	/**
	 * Helper method to get parse results in a wrapper
	 * 
	 * @param sentence
	 *            sentence the String that needs to be parsed
	 * @param lp
	 *            the Parser to be used
	 * @param gsf
	 *            the GrammaticalStructureFactory for this parser
	 * @return a ResultWrapper object
	 */
	public ResultWrapper generateStanfordParserResult(String sentence, LexicalizedParser lp,
			GrammaticalStructureFactory gsf) {

		Tree parse = lp.parse(sentence);

		List<TaggedWord> taggedWords = parse.taggedYield();
		GrammaticalStructure grammaticalStructure = gsf.newGrammaticalStructure(parse);
		List<TypedDependency> typedDependencies = grammaticalStructure
				.typedDependenciesCCprocessed();
		parse.pennPrint();
		System.out.println(taggedWords);
		return new ResultWrapper(parse, taggedWords, typedDependencies);
	}

	/**
	 * ' Helper method of print a list of generic type
	 * 
	 * @param list
	 *            the input list
	 */
	private static <T> void printList(List<T> list) {
		for (T type : list) {
			System.out.println(String.valueOf(type));
		}
	}

	/**
	 * The Result Wrapper Object
	 * 
	 * @author Chirayu Desai
	 *
	 */
	public class ResultWrapper {
		Tree parse;
		List<TaggedWord> taggedWords;
		List<TypedDependency> typedDependencies;

		public ResultWrapper(Tree parse, List<TaggedWord> taggedWords,
				List<TypedDependency> typedDependencies) {
			this.parse = parse;
			this.taggedWords = taggedWords;
			this.typedDependencies = typedDependencies;
		}

		@Override
		public String toString() {
			return "ResultWrapper [parse=" + parse.pennString() + ", taggedWords=" + taggedWords
					+ ", typedDependencies=" + typedDependencies + "]";
		}
	}
}
