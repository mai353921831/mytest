package com.itheima.lucene.index;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.junit.Test;
import org.wltea.analyzer.lucene.IKAnalyzer;

import java.io.File;

/**
 * QueryTest
 *
 * @author lee.siu.wah
 * @version 1.0
 * <p>File Created at 2018-09-17<p>
 */
public class QueryTest {


    /**
     * TermQuery: 关键字查询
     * 查询图书名称域中包含有java的图书。
     * */
    @Test
    public void testTermQuery() throws Exception{
        // 创建查询对象
        Query query = new TermQuery(new Term("bookName", "java"));
        // 搜索方法
        search(query);
    }

    /**
     * NumericRangeQuery 数字范围查询
     * 需求：查询图书价格在80到100之间的图书。
     * (不包含边界值) bookPrice:{80.0 TO 100.0}
     * (包含边界值) bookPrice:[80.0 TO 100.0]
     */
    @Test
    public void testNumericRangeQuery() throws Exception{
        /**
         * String field: 域的名称
         * Double min: 最小边界值
         * Double max: 最大边界值
         * boolean minInclusive: 是否包含最小边界值
         * boolean maxInclusive： 是否包含最大边界值
         */
        //Query query = NumericRangeQuery.newDoubleRange("bookPrice", 80d, 100d, false, false);
        Query query = NumericRangeQuery.newDoubleRange("bookPrice", 80d, 100d, false, true);

        // 搜索方法
        search(query);
    }

    /**
     * BooleanQuery布尔查询,组合多个查询对象
     * 查询图书名称域中包含有java的图书，并且价格在80到100之间（包含边界值）
     */
    @Test
    public void testBooleanQuery() throws Exception{

        // 关键字查询
        Query q1 = new TermQuery(new Term("bookName", "java"));
        /**
         * String field: 域的名称
         * Double min: 最小边界值
         * Double max: 最大边界值
         * boolean minInclusive: 是否包含最小边界值
         * boolean maxInclusive： 是否包含最大边界值
         */
        Query q2 = NumericRangeQuery.newDoubleRange("bookPrice", 80d, 100d, false, true);
        // 布尔查询,组合多个查询对象
        BooleanQuery query = new BooleanQuery();
        query.add(q1, BooleanClause.Occur.MUST); // AND +
        query.add(q2, BooleanClause.Occur.MUST); // AND

        // 搜索方法
        search(query);
    }

    /**
     * QueryParser: 把表达式解析成Query对象
     * 查询图书名称域中包含有java，并且图书名称域中包含有lucene的图书
     */
    @Test
    public void testQueryParser() throws Exception{
        // 创建分词器
        Analyzer analyzer = new IKAnalyzer();
        // 创建QueryParser
        QueryParser queryParser = new QueryParser("bookName", analyzer);

        // 把表达式解析成Query对象 +bookName:java +bookName:lucene
        // Query query = queryParser.parse("bookName:java AND bookName:lucene");

        // +bookName:java -bookName:lucene
        Query query2 = queryParser.parse("bookName:java NOT bookName:lucene");


        // 搜索方法
        search(query2);
    }


    // 查询的私有的方法
    private void search(Query query) throws Exception{
        System.out.println("查询语法：" + query);
        // 创建索引库存储的目录，用于存储索引与文档
        Directory directory = FSDirectory.open(new File("D:/lucene_index"));
        // 创建索引库读取对象，用于把索引加载到内存中
        IndexReader indexReader = DirectoryReader.open(directory);
        // 创建IndexSearcher搜索对象
        // 参数: IndexReader
        IndexSearcher indexSearcher = new IndexSearcher(indexReader);
        /**
         * 搜索方法
         * 第一个参数：query查询对象
         * 第二个参数：int n 得到检索文档排序后的前几个 (10个)
         */
        TopDocs topDocs = indexSearcher.search(query, 10);
        System.out.println("总命中的记录数：" + topDocs.totalHits);
        // 获取分数文档数组
        ScoreDoc[] scoreDocs = topDocs.scoreDocs;

        // ScoreDoc : 包含分数、文档的id
        for (ScoreDoc scoreDoc : scoreDocs){
            System.out.println("-------华丽分割线-------");
            System.out.println("文档id: " + scoreDoc.doc + "\t" + "分档分数：" + scoreDoc.score);

            // 根据文档id获取文档对象
            Document doc = indexSearcher.doc(scoreDoc.doc);
            System.out.println("图书id: "  + doc.get("id"));
            System.out.println("图书名称: "  + doc.get("bookName"));
            System.out.println("图书价格: "  + doc.get("bookPrice"));
            System.out.println("图书图片: "  + doc.get("bookPic"));
            System.out.println("图书描述: "  + doc.get("bookDesc"));
        }
        // 释放资源
        indexReader.close();
    }
}
