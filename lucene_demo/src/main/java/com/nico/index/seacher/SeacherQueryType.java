package com.nico.index.seacher;

import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.classic.QueryParser.Operator;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.SortedNumericSortField;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TermRangeQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.grouping.GroupDocs;
import org.apache.lucene.search.grouping.GroupingSearch;
import org.apache.lucene.search.grouping.TopGroups;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.Version;
import org.junit.Test;

import com.hankcs.lucene.HanLPAnalyzer;
import com.nico.index.TestData;
import com.nico.util.LuceneUtil;

/**
 * 
 * @Title: lucene seacher demo
 * @Package com.nico.index.seacher  
 * @Description: 
 * @author fangshu  
 * @date 2018年6月21日  
 * @version
 */
public class SeacherQueryType extends TestData {
	/**
	 * 缓存Directory
	 */
	private static Map<String,Directory> directorys=new HashMap<String, Directory>();
	/**
	 * 缓存indexWriters
	 */
	private static  Map<String,IndexWriter> indexWriters=new HashMap<String, IndexWriter>();
	/**
	 * 缓存indexReaders
	 */
	private static  Map<String,IndexReader> indexReaders=new HashMap<String, IndexReader>();
	/**
	 * 解析器
	 */
	private static Analyzer analyzer= new HanLPAnalyzer();
	
	
	/**
	 * 
	 * @Title: 获取searcher
	 * @Description: 
	 * 多次OPEN indexReader 会有比较大的开销，一般重用indexReader
	 * @date 2018年6月21日  
	 * @return        
	 * @throws
	 */
	public static IndexSearcher getIndexSearcher(String path){
		IndexReader indexReader=null;
		if(indexReaders.get(path)!=null){
			 //如果存在就重用
			 indexReader = LuceneUtil.getIndexReader(indexReaders.get(path));
		}else{
			Directory openDirectory=null;
			
			if(directorys.get(path)!=null){
				openDirectory=directorys.get(path);
				
			}else{
				openDirectory = LuceneUtil.openDirectory(path);
				directorys.put(path, openDirectory);
			}
			//如果不存在就新建
			indexReader=LuceneUtil.getIndexReader(openDirectory, analyzer);
			indexReaders.put(path, indexReader);
		}
		
		return new IndexSearcher(indexReader);
	}
	
	
	/**
	 * 指定项搜索
	 * @throws Exception
	 */
	@Test
	public void testTermQuery()throws Exception{
		IndexSearcher is = getIndexSearcher(INDEX_PATH);
		
		BooleanQuery.Builder booleanQuery=new BooleanQuery.Builder();
		//lucene搜索分分词和不分词2种情况，分词时每一个都是  不分词时，必须得写全才能搜索到
		//同时每一种搜索类都会决定最终是分词还是不分词--如TermQuery就不会分词
		Query companyIdQuery = new TermQuery(new Term("title2","南京java SeacherDemo")); 
		booleanQuery.add(companyIdQuery,BooleanClause.Occur.MUST); 
		TopDocs hits=is.search(booleanQuery.build(), 10);
		for(ScoreDoc scoreDoc:hits.scoreDocs){
			Document doc=is.doc(scoreDoc.doc);
			System.out.println(doc.get("id"));
			System.out.println(doc.get("title"));
		}		
	}
	
	
	/**
	 * 分组查询--需要先将某个字段列入分组，才能进行分组查询
	 */
	@Test
	public void testGroupQuery()throws Exception{
		String groupField="group3";
		IndexSearcher is = getIndexSearcher(INDEX_PATH);
		//
		GroupingSearch groupingSearch = new GroupingSearch(groupField);
		//正序
		Sort sort=new Sort(new SortField("group3",SortField.Type.STRING ,false));
		//倒序
		//Sort sort=new Sort(new SortField("group3",SortField.Type.STRING ,false));
		//组间排序
	    //groupingSearch.setGroupSort(new Sort(SortField.FIELD_SCORE));
		groupingSearch.setGroupSort(sort);
	    //组内排序
	    groupingSearch.setSortWithinGroup(new Sort(SortField.FIELD_SCORE));
	    groupingSearch.setFillSortFields(true);
	   //第一次查询缓存容量的大小：设置为16M
	    groupingSearch.setCachingInMB(16.0, true);
	    groupingSearch.setAllGroups(true);
	    //groupingSearch.setAllGroupHeads(true);
	    groupingSearch.setGroupDocsLimit(10);
	 
	    String queryWords="江西 南京 中国 东部";
	    QueryParser queryParser=new QueryParser("content",new HanLPAnalyzer());
	    Query query = queryParser.parse(queryWords);
 
	    TopGroups<BytesRef> result = groupingSearch.search(is, query, 0, 1000);
	 
	    //总命中数
        System.out.println("总命中数:"+result.totalHitCount);
        //分组数
        System.out.println("分组数:"+result.groups.length);
        //按照分组打印查询结果
        for (GroupDocs<BytesRef> groupDocs : result.groups){
        	System.out.println("组内数据条数:" + groupDocs.totalHits);
        	if (groupDocs.groupValue != null) {
                System.out.println("分组:" + groupDocs.groupValue.utf8ToString());
            }else{
            	System.out.println("组内groupValue:" + groupDocs.groupValue);
            }
        	
        	System.out.println("组内toString:" + groupDocs.toString());
        	for(ScoreDoc scoreDoc : groupDocs.scoreDocs){
                Document doc=is.doc(scoreDoc.doc);
                System.out.println(doc.get("id"));
                System.out.println(doc.get("title"));
            }
		}		
	}
	
	
	/**
	 * 指定项范围搜索
	 * @throws Exception
	 */
	@Test
	public void testTermRangeQuery()throws Exception{
		IndexSearcher is = getIndexSearcher(INDEX_PATH);
		TermRangeQuery query=new TermRangeQuery("desc", new BytesRef("b".getBytes()), new BytesRef("c".getBytes()), true, true);
		TopDocs hits=is.search(query, 10);
		for(ScoreDoc scoreDoc:hits.scoreDocs){
			Document doc=is.doc(scoreDoc.doc);
			System.out.println(doc.get("id"));
			System.out.println(doc.get("city"));
			System.out.println(doc.get("desc"));
		}		
	}
	
	/**
	 * 指定数字范围
	 * @throws Exception
	 */
	@Test
	public void testNumericRangeQuery()throws Exception{
		IndexSearcher is = getIndexSearcher(INDEX_PATH);
		NumericRangeQuery<Integer> query=NumericRangeQuery.newIntRange("id", 1, 2, true, true);
		TopDocs hits=is.search(query, 10);
		for(ScoreDoc scoreDoc:hits.scoreDocs){
			Document doc=is.doc(scoreDoc.doc);
			System.out.println(doc.get("id"));
			System.out.println(doc.get("city"));
			System.out.println(doc.get("desc"));
		}		
	}
	
	/**
	 * 指定字符串开头搜索
	 * @throws Exception
	 */
	@Test
	public void testPrefixQuery()throws Exception{
		IndexSearcher is = getIndexSearcher(INDEX_PATH);
		PrefixQuery query=new PrefixQuery(new Term("city","a"));
		TopDocs hits=is.search(query, 10);
		for(ScoreDoc scoreDoc:hits.scoreDocs){
			Document doc=is.doc(scoreDoc.doc);
			System.out.println(doc.get("id"));
			System.out.println(doc.get("city"));
			System.out.println(doc.get("desc"));
		}	
	}
	
	/**
	 * 多条件查询--跨filed
	 * @throws Exception
	 */
	@Test
	public void testBooleanQuery()throws Exception{
		IndexSearcher is = getIndexSearcher(INDEX_PATH);
		NumericRangeQuery<Integer> query1=NumericRangeQuery.newIntRange("id", 1, 2, true, true);
		PrefixQuery query2=new PrefixQuery(new Term("city","a"));
		BooleanQuery.Builder booleanQuery=new BooleanQuery.Builder();
		booleanQuery.add(query1,BooleanClause.Occur.MUST);
		booleanQuery.add(query2,BooleanClause.Occur.MUST);
		TopDocs hits=is.search(booleanQuery.build(), 10);
		for(ScoreDoc scoreDoc:hits.scoreDocs){
			Document doc=is.doc(scoreDoc.doc);
			System.out.println(doc.get("id"));
			System.out.println(doc.get("city"));
			System.out.println(doc.get("desc"));
		}	
	}
	
	/**
	 * 
	 * @Title: 一个字段上查询多个关键字--使用语法OR就可以了
	 * @Description: 并不是跨field查询
	 * @date 2018年6月22日  
	 * @throws Exception        
	 * @throws
	 */
	@Test
	public void seacherCrossField() throws Exception{
		IndexSearcher indexSearcher = getIndexSearcher(INDEX_PATH);
		//MultiFieldQueryParser multiFieldQueryParser=new MultiFieldQueryParser(new String[]{"name","content"},analyzer);
		//Query parse = multiFieldQueryParser.parse("广州省");
		Query parse = MultiFieldQueryParser.parse(new String[]{"广州","广州"}, new String[]{"content","content"},new BooleanClause.Occur[]{BooleanClause.Occur.MUST,BooleanClause.Occur.MUST}, analyzer);
		
		TopDocs search = indexSearcher.search(parse, 10);
		System.out.println(search.totalHits);
		for (ScoreDoc scoreDoc: search.scoreDocs) {
			Document doc = indexSearcher.doc(scoreDoc.doc);
			System.out.println(doc.get("id"));
			System.out.println(doc.get("name"));
			System.out.println(doc.get("title"));
			System.out.println(doc.get("content"));
			System.out.println(doc.get("contents_no_save"));
		}
		
	}
	
	/**
	 * 
	 * @Title: SQL一样的查询
	 * @Description: 
	 * 官方介绍 http://lucene.apache.org/core/3_0_3/queryparsersyntax.html
	 * @date 2018年6月22日  
	 * @throws Exception        
	 * @throws
	 */
	@Test
	public void seacherSqlMode() throws Exception{
		QueryParser queryParser = new QueryParser("contents_string", analyzer);

		queryParser.setAllowLeadingWildcard(true);//设置允许通配符？和*就可以使用
//		queryParser.setDefaultOperator(Operator.AND);//默认or
		
//		这里的str有很多，
//
//		1.“我是中国人”，就是从desc中查找有“我是中国人”的返回。跟termQuery差不多。
//
//		2.“id:[1 TO 3]”,查找id从1到3 的返回，中括号包含两边，大括号不包含。跟范围查找差不多
//
//		3.“city:北京”，查找city为北京的返回，不从默认字段desc中查
//
//		4.“hello~0.1”,模糊查询，~后面是0到1之间，靠近0就是跟hello很不一样，靠近1就是差不多一样
//
//		5.“\"hello  world\"~3”，片段查询，查hello跟world之间有3个词的。
//
//		6“+city:北京  -id:1”  ，查询city为北京，但是id不为1的。
//
//		7“city:?京*”，？是一个字符，*是多个字符，这是查  "x京xxxxxxx"的city
//
//		8.“北京 南京”，查找content中有北京或者有南京的，中间默认是or，queryParser.setDefaultOperator(Operator.AND);可以设置为and,或者 “北京 and 南京”

//		9.“北京 AND 南京” 查找content中有北京和南京的，要大写，小写不算
		
//		10.“北京 OR 南京” 查找content中有北京或者有南京的，要大写，小写不算
		
		String str="江西*中国*";
		Query query = queryParser.parse(str); 
		IndexSearcher indexSearcher = getIndexSearcher(INDEX_PATH);
		TopDocs search = indexSearcher.search(query, 10);
		System.out.println(search.totalHits);
		for (ScoreDoc scoreDoc : search.scoreDocs) {
			Document doc = indexSearcher.doc(scoreDoc.doc);
			System.out.println(doc.get("id"));
			System.out.println(doc.get("name"));
			System.out.println(doc.get("title"));
			System.out.println(doc.get("content"));
			System.out.println(doc.get("contents_no_save"));
		}
		
	}
	
}
