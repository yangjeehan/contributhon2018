package com.onycom.crawler.analysis;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.onycom.crawler.process.KeywordMatrixForAsso;

import weka.associations.AssociationRule;
import weka.associations.AssociationRules;
import weka.associations.FPGrowth;
import weka.core.Instances;
import weka.core.converters.CSVLoader;
import weka.core.converters.ConverterUtils.DataSource;

public class AssociationRuleGenerater {
	public static void main(String[] args){
		AssociationRuleGenerater ar = new AssociationRuleGenerater();
		KeywordMatrixForAsso mat = new KeywordMatrixForAsso();
		mat.generateMatrix();
		ar.core();
	}
	public void core(){ 
		//file Read
		
		DataSource source; 
		try {
//			source = new DataSource("/conv/sbs-matrix-20161216.csv");
			CSVLoader loader = new CSVLoader();
			loader.setSource(new File("/conv/sbs-matrix-20161216.csv"));

			String[] options = new String[2]; 
//			options[0] = "-N";                                    // "range"
//			options[1] = "first-last";
//			loader.setOptions(options);

			Instances data = loader.getDataSet();
			
//			Instances data = source.getDataSet();
			int len = data.numAttributes();
//			
//			
//			
//			StringBuffer buf = new StringBuffer();
//			for(int i =0; i < len ; i++){
////				System.out.println(data.attribute(i).isNominal());
//				if(!data.attribute(i).isNominal()){
//					buf.append((i+1));
//					buf.append(",");
//				}
//			}
//			
//			options = new String[2];
//			
//			options[0] = "-R";                                    // "range"
//
//			
//			System.out.println(options[1]);
//			Remove rm = new Remove();
//			
//			RemoveType rmtype = new RemoveType();
//			options[0] = "-T"; 
//			options[1] = "string";
//			rmtype.setOptions(options);
//
//			data = Filter.useFilter(data, rmtype);
			
//			
//			NumericToBinary ntb = new NumericToBinary();
//			StringToNominal stn = new StringToNominal();
//			
//			options = new String[2];
//			
//			options[0] = "-R";                                    // "range"
//			options[1] = "first-last";
//			
//			ntb.setOptions(options);
//			ntb.input(data.firstInstance());

			int size = data.size();
			
			System.out.println(len);
			for(int i =0; i < len ; i++){
				System.out.println(data.attribute(i));
			}
			
			FPGrowth fp = new FPGrowth();
			options = new String[11];
			options[0] = "-C";
			options[1] = "0.3";
			options[2] = "-M";
			options[3] = "0.001";
			options[4] = "-rules";
			options[5] = "가짜,곰팡이,공업용,과대광고,기생충란,기생충,납,노로바이러스,농약,다이옥신,대장균,리스테리아,말라카이트,멜라민,물량빼기,물코팅,미끼용,밀수약품,반품,발암,발암물질,방사능,방사선조사,벤젠,벤조피렌,불량,비위생 ,사료용,사용금지,사카자키균,살모넬라,살충제,상한,석유냄새,세균,센노사이드,쇳가루,수단색소,수은,시부트라민,식재료,식중독,쓰레기,아크릴아마이드,알레르기,알루미늄,얼음옷,에탄올,오염,위생관리,위생불량,위생실태,유전자변형,이물,이물질,이엽우피소,이취,악취,재사용,재판매,저질,중금속,코카인,클로스트리디움,타르색소,탄저병,폐기대상,폐기물,폐기용,표백제,합성감미료,항생제,허위,변질,거짓,위반";
			options[6] = "-use-or";
			options[7] = "-N";
			options[8] = "10";
			options[9] = "-D";
			options[10] = "0.2";
			
			fp.setOptions(options);
			fp.buildAssociations(data);
			AssociationRules rules = fp.getAssociationRules();
			
			List<AssociationRule> ruleList = rules.getRules();
			for(int i = 0; i < ruleList.size(); i++){
				System.out.println(ruleList.get(i).toString());
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
