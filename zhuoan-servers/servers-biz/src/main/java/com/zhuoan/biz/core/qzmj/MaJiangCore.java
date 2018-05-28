package com.zhuoan.biz.core.qzmj;

import com.zhuoan.biz.model.qzmj.DontMovePai;
import com.zhuoan.constant.QZMJConstant;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang.ArrayUtils;

import java.util.*;

public class MaJiangCore {
	
	private static int yunsuanCount = 0;

	public static void main(String[] args) {
		
		long yunxingCount = 1000000000;
		int huCount = 0;
		for (int i = 0; i < yunxingCount; i++) {

			if(testHu()){
				huCount ++;
				System.out.println(i);
			}
		}
		System.out.println("运行次数："+yunxingCount+"，胡次数："+huCount);
	}
	
	/**
	 * 麻将胡牌规则测试方法
	 * @return 
	 */
	private static boolean testHu(){
		

		int[] indexs = randomPai(QZMJConstant.ALL_PAI.length);
		Integer[] pai = new Integer[QZMJConstant.ALL_PAI.length];
		
		for (int i = 0; i < indexs.length; i++) {
			
			pai[i] = QZMJConstant.ALL_PAI[indexs[i]];
		}
		
		Integer[] myPai=Arrays.copyOfRange(pai, 0, 14);
		
		return isHu(Arrays.asList(myPai), 0, 11);
	}
	
	/**
	 * 打乱牌的下标
	 * @param paiCount
	 * @return
	 */
	private static int[] randomPai(int paiCount){
		
		int[] nums = new int[paiCount];
		Random rd = new Random();
		for (int i = 0; i < nums.length; i++) {
			while(true){
				int num = rd.nextInt(paiCount);
                // 开局庄家手牌不带花
                if(i<QZMJConstant.LEFT_PAI_COUNT){
					if(!QZMJConstant.isHuaPai(QZMJConstant.ALL_PAI[num]) && !ArrayUtils.contains(nums,num)){
						nums[i] = num;
						break;
					}
				}else if(!ArrayUtils.contains(nums,num)){
					nums[i] = num;
					break;
				}else if(num==0){
					if(ArrayUtils.indexOf(nums, num) == i){
						break;
					}
				}
			}
		}
		return nums;
	}


    /**
     * 判断杠的核心方法
     * @param myPai
     * @param otherPai
     * @param type
     * @param penghistory
     * @return
     */
	public static int[] isGang(List<Integer> myPai,Integer otherPai,int type,List<DontMovePai> penghistory){
		
		
		int paiCount = tongPaiCount(myPai, otherPai);
		
		if(type==1){
			
			if(paiCount==4){  // 暗杠
				return new int[]{3, otherPai, otherPai, otherPai, otherPai};
			}else{ // 补杠
				for (DontMovePai p : penghistory) {
					if(otherPai.equals(p.getFoucsPai())){
						return new int[]{1, otherPai, otherPai, otherPai};
					}
				}
			}
			
			// 判断手牌是否可以杠 
			for (int pai : myPai) {
				// 暗杠
				if(tongPaiCount(myPai, pai)==4){
					return new int[]{3, pai, pai, pai, pai};
				}
				// 补杠
				for (DontMovePai p : penghistory) {
					if(pai == p.getFoucsPai()){
						return new int[]{1, pai, pai, pai};
					}
				}
			}
		}else{
            // 明杠
            if(paiCount==3){
				return new int[]{2, otherPai, otherPai, otherPai};
			}else if(penghistory!=null){
                // 特殊情况
				for (DontMovePai p : penghistory) {
					if(otherPai.equals(p.getFoucsPai())){
						return new int[]{1, otherPai, otherPai, otherPai};
					}
				}
			}
		}
		
		return new int[]{0};
	}
	
	
	/**
	 * 获取同样的牌的数量
	 * @param myPai
	 * @param otherPai
	 * @return
	 */
	public static int tongPaiCount(List<Integer> myPai,Integer otherPai) {
		
		List<Integer> paiList = new ArrayList<Integer>();
		for (int pai : myPai) {
			if(otherPai.equals(pai)){
				paiList.add(pai);
			}
		}
		return paiList.size();
	}

    /**
     * 判断碰的核心方法
     * @param myPai
     * @param otherPai
     * @return
     */
	public static int[] isPeng(List<Integer> myPai,Integer otherPai){
		
		List<Integer> pengList = new ArrayList<Integer>();
		for (int pai : myPai) {
			if(otherPai.equals(pai)){
				pengList.add(pai);
			}
		}
		if(pengList.size()>=2){
			return new int[]{1, pengList.get(0), pengList.get(1)};
		}
		return new int[]{0};
	}

    /**
     * 判断吃的核心方法
     * @param myPai
     * @param otherPai
     * @param jin
     * @return
     */
	public static List<int[]> isChi(List<Integer> myPai,Integer otherPai, int jin){
		
		//1.查询otherPai是否属于可吃的牌
		List<Integer> zhupai=null;
		if(QZMJConstant.TONG_PAI.contains(otherPai)){
			zhupai=QZMJConstant.TONG_PAI;
		}else if(QZMJConstant.TIAO_PAI.contains(otherPai)){
			zhupai=QZMJConstant.TIAO_PAI;
		}else if(QZMJConstant.WANG_PAI.contains(otherPai)){
			zhupai=QZMJConstant.WANG_PAI;
		}
		if(zhupai!=null&&zhupai.size()>0){
			//获取牌的位置
			int index=zhupai.indexOf(otherPai);
			int maxLength=zhupai.size();
			//获取组合
			int a=index-2>=0?zhupai.get(index-2):-1;
			int b=index-1>=0?zhupai.get(index-1):-1;
			int e=index+1<maxLength?zhupai.get(index+1):-1;
			int f=index+2<maxLength?zhupai.get(index+2):-1;
			List<int[]> chiList=new ArrayList<int[]>();
			if(myPai.contains(a)&&myPai.contains(b)){
				// 金牌不提示吃
				if(a!=jin && b!=jin){
					chiList.add(new int[]{a,b});
				}
			}
			if(myPai.contains(b)&&myPai.contains(e)){
				// 金牌不提示吃
				if(e!=jin && b!=jin){
					chiList.add(new int[]{b,e});
				}
			}
			if(myPai.contains(e)&&myPai.contains(f)){
				// 金牌不提示吃
				if(e!=jin && f!=jin){
					chiList.add(new int[]{e,f});
				}
			}
			return chiList;
		}
		return null;
	}
	
	
	/**
	 * 判断胡的核心方法
	 * @param myPai 玩家牌组
	 * @param otherPai 胡的牌（自摸时值为0）
	 * @param jinPai
	 * @return
	 */
	public static boolean isHu(List<Integer> myPai, Integer otherPai, Integer jinPai){

		List<Integer> paiList = new ArrayList<Integer>(myPai);
		List<Integer> jinList = new ArrayList<Integer>();
		
		for (int i : paiList) {
			
			if(jinPai.equals(i)){
				
				jinList.add(i);
			}
		}
		
		paiList.removeAll(jinList);

		// 三金倒（自己摸牌时）
		if(otherPai.equals(0)){
			if(jinList.size()>=3){
				return true;
			}
		}
		boolean isCanhu = isCanHU(paiList, jinList, otherPai);
		return isCanhu;
	}
	
	
	/**
	 * 判断牌是否可以胡
	 * @param paiList
	 * @param jinList
	 * @param pai
	 * @return
	 */
	public static boolean isCanHU(List<Integer> paiList, List<Integer> jinList, int pai) {
		
	    List<Integer> pais = new ArrayList<Integer>(paiList);
	    if(pai!=0){
	    	pais.add(pai);
	    }

		// 不满足n%3==2，则不能胡牌
		if((pais.size()+jinList.size())%3!=2){
			return false;
		}
		
	    //只有两张牌
	    if (pais.size() == 2) {
	        return pais.get(0).equals(pais.get(1));
	    }
	 
	    //牌序
	    Collections.sort(pais);
	    
	    //依据牌的顺序从左到右依次分出将牌
	    for (Integer i = 0; i < pais.size(); i++) {
	    	
	    	List<Integer> paiT = new ArrayList<Integer>(pais);
	    	List<Integer> jinPaiList = new ArrayList<Integer>(jinList);
	    	List<Integer> ds = new ArrayList<Integer>();
	    	
	    	for (Integer j = 0; j < pais.size(); j++){
	    		
	    		if(pais.get(i).equals(pais.get(j))){
	    			ds.add(pais.get(i));
	    		}
	    	}
	    	
	    	//判断是否能做将牌
	    	if (ds.size() >= 2) {
	    		
	    		//移除两张将牌
	    		paiT.remove(pais.get(i));
	    		paiT.remove(pais.get(i));
	    		
	    		//避免重复运算 将光标移到其他牌上（跳一步）
	    		i += 1;
	    		
	    		if (HuPaiPanDin(paiT,jinPaiList)) {
	    			return true;
	    		}
	    	}
	    }
	    
	    if(jinList.size()>0){
	    	
	    	//依据牌的顺序从左到右依次分出将牌
	    	for (Integer i = 0; i < pais.size(); i++) {
	    		
	    		List<Integer> paiT = new ArrayList<Integer>(pais);
	    		List<Integer> jinPaiList = new ArrayList<Integer>(jinList);
	    		if(jinList.size()>0&&i < pais.size()){
	    			
	    			//移除一张牌
	    			paiT.remove(pais.get(i));
	    			//一张金牌做将牌
	    			jinPaiList.remove(jinPaiList.get(0));
	    			
	    			if (HuPaiPanDin(paiT,jinPaiList)) {
	    				return true;
	    			}
	    		}
	    	}
	    }
	 
	    return false;
	}
	
	
	/**
	 * 胡牌判定
	 * @param paiList
	 * @param jinList 
	 * @return
	 */
	private static boolean HuPaiPanDin(List<Integer> paiList, List<Integer> jinList) {

		yunsuanCount ++ ;
	    
		List<Integer> paiNewList = new ArrayList<Integer>(paiList);
		List<Integer> jinNewList = new ArrayList<Integer>(jinList);
		
	    if (paiList.size() == 0) {
	        return true;
	    }
	 
	    List<Integer> fs = new ArrayList<Integer>();
        for (Integer i = 0; i < paiList.size(); i++){
			
        	if(paiList.get(i).equals(paiList.get(0))){
        		fs.add(paiList.get(0));
        	}
		}
        
	    //组成刻子
	    if (fs.size() == 3) {
	      
	    	paiList.remove(paiList.get(0));
	        paiList.remove(paiList.get(0));
	        paiList.remove(paiList.get(0));
	 
	        return HuPaiPanDin(paiList, jinList);
	        
	    }else{ //组成顺子（排除字牌）
	    	
	    	Integer p1 = paiList.get(0) + 1;
	    	Integer p2 = paiList.get(0) + 2;
	    	
	        if (!QZMJConstant.ZI_PAI.contains(paiList.get(0))) {
	        	if(paiList.contains(p1) && paiList.contains(p2)){
	        		
	        		paiList.remove(paiList.get(0));
	        		paiList.remove(p1);
	        		paiList.remove(p2);
	        		
	        		if(HuPaiPanDin(paiList, jinList)){
	        			return true;
	        		}
	        	}
	        }
	        
	        /**
	         * 20171009 lhp 
	         * 用金代替其他牌（进入一个新的判断流程）
	         */
	        if(paiNewList.size()>0 && jinNewList.size()>0){ 
	        	
	        	fs.clear();
	        	for (Integer i = 0; i < paiNewList.size(); i++){
	    			
	            	if(paiNewList.get(i).equals(paiNewList.get(0))){
	            		fs.add(paiNewList.get(0));
	            	}
	    		}
	        	
	        	if(fs.size() == 2 && jinNewList.size()>=1){
	        	    // 组成刻子,带一个金
	        		List<Integer> pList = new ArrayList<Integer>(paiNewList);
	    	    	pList.remove(pList.get(0));
	    	        pList.remove(pList.get(0));

                    // 移除金牌
                    jinNewList.remove(jinNewList.get(0));

	    	    	return HuPaiPanDin(pList, jinNewList);
	    	        
	        	}else if (!QZMJConstant.ZI_PAI.contains(paiNewList.get(0))){
	        	    // 用金代替其他牌，组成顺子（不能包含字牌）
	    	    	
	    	    	// 当顺子的牌不能被10整除，且要是同类型的牌
	    	    	if (p1%10==0 || paiNewList.get(0)/10 != p1/10){ 
	    	    		p1 = p1-3;
	    	    	}
	    	    	
	    	    	if (p2%10==0 || paiNewList.get(0)/10 != p2/10){ 
	    	    		p2 = p2-3;
	    	    	}
	    	    	
	    	    	if(!paiNewList.contains(p1) && paiNewList.contains(p2) && jinNewList.size()>=1){

	    	    	    // 带一个金
	    	    		List<Integer> pList = new ArrayList<Integer>(paiNewList);
	    	    		pList.remove(pList.get(0));
	    	    		pList.remove(p2);

                        // 移除金牌
                        jinNewList.remove(jinNewList.get(0));

	    	    		return HuPaiPanDin(pList, jinNewList);
	    	    		
	    	    	}else if(paiNewList.contains(p1) && !paiNewList.contains(p2) && jinNewList.size()>=1){
	    	    	    // 带一个金
	    	    		List<Integer> pList = new ArrayList<Integer>(paiNewList);
	    	    		pList.remove(pList.get(0));
	    	    		pList.remove(p1);

                        // 移除金牌
                        jinNewList.remove(jinNewList.get(0));

	    	    		return HuPaiPanDin(pList, jinNewList);
	    	    		
	    	    	}else if(jinNewList.size()>=2){
	    	    	    // 带两个金
	    	    		List<Integer> pList = new ArrayList<Integer>(paiNewList);
	    	    		pList.remove(pList.get(0));
                        // 移除金牌
	    	    		jinNewList.remove(jinNewList.get(0));
                        // 移除金牌
	    	    		jinNewList.remove(jinNewList.get(0));
	    	    		return HuPaiPanDin(pList, jinNewList);
	    	    	}
	    	    }
	        }
	        
	        return false;
	    }
	}
	
	
	/**
	 * 判断玩家是否听牌
	 * @param myPai
	 * @param jinPai
	 * @return
	 */
	public static List<Integer> isTingPai(List<Integer> myPai, Integer jinPai){
		
		List<Integer> paiList = new ArrayList<Integer>(myPai);
		List<Integer> jinList = new ArrayList<Integer>();
		
		for (int i : paiList) {
			
			if(jinPai.equals(i)){
				
				jinList.add(i);
			}
		}
		
		paiList.removeAll(jinList);
		
		List<Integer> result = new ArrayList<Integer>();
		
		for (int i : QZMJConstant.ALL_CAN_HU_PAI) {
			
			if(isCanHU(paiList, jinList, i)){
				result.add(i);
			}
		}
		return result;
	}

    /**
     * 听牌（游金）提示
     * @param pais
     * @param jinPai
     * @param shengyuList
     * @return
     */
	public static JSONArray tingPaiTip(List<Integer> pais, Integer jinPai, List<Integer> shengyuList){

		// 方法开始执行时间
		long startTime = System.currentTimeMillis();
		yunsuanCount=0;
		JSONArray array = new JSONArray();
		try {
			
			// 遍历玩家手牌
			for (Integer pai:pais) {
				
				List<Integer> paiList = new ArrayList<Integer>(pais);
                // 模拟打牌
                paiList.remove(pai);
				JSONObject result = youJinAndTingPaiPanDing(paiList, jinPai, shengyuList);
				if(result!=null){
					
					// 如果听的牌没有包含金牌，则计算剩余金牌的数量
					if(result.getInt("type")==0){
						
						JSONArray vals = result.getJSONArray("values");
						boolean hasJin = false;
						for (int i = 0; i < vals.size(); i++) {
							if(jinPai.equals(vals.getJSONObject(i).getInt("val"))){
								hasJin = true;
								break;
							}
						}
                        // 计算可听剩余金牌的数量
                        if(!hasJin){
                            // 金牌最多三张
                            int count = -1;
							for (Integer p : shengyuList) {
								if(jinPai.equals(p)){
									count++;
								}
							}
							if(count>=0){
								
								JSONObject value = new JSONObject();
								value.put("val", jinPai);
								value.put("count", count);
								vals.add(value);
								
								result.put("values", vals);
							}
						}
					}
					
					result.put("pai", pai);
					array.add(result);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			
		}
		return array;
	}
	
	
	/**
	 * 听牌（游金）判定
	 * @param paiList
	 * @param jin
	 * @return
	 */
	public static JSONObject youJinAndTingPaiPanDing(List<Integer> paiList, Integer jin, List<Integer> shengyuList){
		
		//牌序
	    Collections.sort(paiList);
		
		int type = 0;
		
		// 获取所有可以听的牌
		List<Integer> valList = isTingPai(paiList, jin);
		
		// 所有牌都可以听，则说明可游金
		if(valList.size()==QZMJConstant.ALL_CAN_HU_PAI.length){
			type = 1;
		}
		
		if(valList.size()>0){
			
			JSONObject obj = new JSONObject();
			obj.put("type", type);
			if(type==0&&shengyuList!=null){
				JSONArray values = new JSONArray();
				for (Integer pai : valList) {
					int count = 0;
					for (Integer p : shengyuList) {
						if(pai.equals(p)){
							count++;
						}
					}
					// 金牌数量-1
					if(pai.equals(jin)){
						count = count-1;
					}
					JSONObject value = new JSONObject();
					value.put("val", pai);
					value.put("count", count);
					values.add(value);
				}
				obj.put("values", values);
			}
			return obj;
		}
		return null;
	}
	

	/**
	 * 胡牌类型判断（3n+2）
	 * @param myPai 牌组
	 * @param otherPai 判断的牌（当huType为1时，值为0）
	 * @param jinPai 金牌
	 * @param huType 1.自摸    2.普通糊
	 * @return
	 */
	public static int huPaiType(List<Integer> myPai,Integer otherPai,Integer jinPai, int huType){
		
		// 自摸且带金，判断是否可以游金
		if(myPai.contains(jinPai) && huType==1){
			
			return huPaiHasJin(myPai, 0, jinPai);
			
		}else{

            // 自摸
            if(huType==1){

				if(isHu(myPai, 0, jinPai)){
					return QZMJConstant.HU_TYPE_ZM;
				}
			}else{
                // 平胡
		
				if(isHu(myPai, otherPai, jinPai)){
					return QZMJConstant.HU_TYPE_PH;
				}
			}
		}
		
		return 0;
	}
	
	
	/**
	 * 带金的胡牌类型
	 * @param pais
	 * @param pai
	 * @param jin
	 * @return
	 */
	public static int huPaiHasJin(List<Integer> pais, Integer pai, Integer jin){
		try {
			List<Integer> paiList = new ArrayList<Integer>(pais);
			
			 //牌序
			Collections.sort(paiList);
			
			// 获取金牌
			List<Integer> jinList = new ArrayList<Integer>();
			for (int i : paiList) {
				if(jin.equals(i)){
					jinList.add(i);
				}
			}

            // 移除金牌
            paiList.removeAll(jinList);

			// 有金牌
			if(jinList.size()>0){
				
				if(isCanYouJin(paiList, jinList, pai)){
                    // 游金
                    return QZMJConstant.HU_TYPE_YJ;
				}
			}
			
			// 三金倒（自己摸牌时）
			if(pai==0){
				if(jinList.size()>=3){
                    // 三金倒
                    return QZMJConstant.HU_TYPE_SJD;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally{

		}
		
		return QZMJConstant.HU_TYPE_ZM;
	}
	

	/**
	 * 判断是否可以游金
	 * @param paiList 手牌
	 * @param jinList
	 * @param pai
	 * @return
	 */
	public static boolean isCanYouJin(List<Integer> paiList, List<Integer> jinList, int pai) {
		

	    List<Integer> pais = new ArrayList<Integer>(paiList);
	    if(pai!=0){
	    	pais.add(pai);
	    }
	    //只有两张将牌且带金
	    if ((pais.size() == 0 && jinList.size()==2) || (pais.size() == 1 && jinList.size()==1)) {
	        return true;
	    }
	 
	    //牌序
	    Collections.sort(pais);
	 
	    //依据牌的顺序从左到右依次分出将牌
	    for (Integer i = 0; i < pais.size(); i++) {
	    	
	        List<Integer> paiT = new ArrayList<Integer>(pais);
	        List<Integer> jinPaiList = new ArrayList<Integer>(jinList);
	        List<Integer> ds = new ArrayList<Integer>();
	        
	        for (Integer j = 0; j < pais.size(); j++){
				
	        	if(pais.get(i).equals(pais.get(j))){
	        		ds.add(pais.get(i));
	        	}
			}
	 
	        //判断是否能做将牌
	        if (ds.size() >= 2) {
	        	
	            //移除两张将牌
	            paiT.remove(pais.get(i));
	            paiT.remove(pais.get(i));
	 
	            //避免重复运算 将光标移到其他牌上（跳一步）
	            i += 1;
	 
	            if (HuPaiPanDin(paiT,jinPaiList)) {
	            	if(jinPaiList.size()>0 || paiT.size()%3==1){ // 剩下最后一张牌为将牌
	            		
	            		return true;
	            	}
	            }
	        }
	    }
	    
	    if(jinList.size()>0){
	    	
	    	//依据牌的顺序从左到右依次分出将牌
	    	for (Integer i = 0; i < pais.size(); i++) {
	    		
	    		List<Integer> paiT = new ArrayList<Integer>(pais);
	    		List<Integer> jinPaiList = new ArrayList<Integer>(jinList);
	    		if(jinList.size()>0&&i < pais.size()){
	    			
	    			//移除一张牌
	    			paiT.remove(pais.get(i));
	    			//一张金牌做将牌
	    			jinPaiList.remove(jinPaiList.get(0));
	    			
	    			if (HuPaiPanDin(paiT,jinPaiList)) {
	    				return true;
	    			}
	    		}
	    	}
	    }
	    
	    return false;
	}
	
	
	/**
	 * 双游、三游判定
	 * @param pais
	 * @param pai
	 * @param jin
	 * @return
	 */
	public static boolean shuangSanYouPanDing(List<Integer> pais, Integer pai, Integer jin){
		
		List<Integer> paiList = new ArrayList<Integer>(pais);
		
		//牌序
		Collections.sort(paiList);
		
		// 获取金牌
		List<Integer> jinList = new ArrayList<Integer>();
		for (int i : paiList) {
			if(jin.equals(i)){
				jinList.add(i);
			}
		}
		
		// 金牌做对子，可以双、三游
		if(jinList.size()>=2){
			
			paiList = new ArrayList<Integer>(pais);
			// 移除两张金牌
			paiList.remove(jin);
			paiList.remove(jin);
			
			paiList.add(0);
			paiList.add(0);
			
			if(isHu(paiList, pai, jin)){
				return true;
			}
		}

		return false;
	}
	
}