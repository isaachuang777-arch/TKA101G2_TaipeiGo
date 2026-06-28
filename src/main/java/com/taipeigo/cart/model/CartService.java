package com.taipeigo.cart.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.taipeigo.customer.model.CustomerVO;
import com.taipeigo.product.dto.CartItemDTO;
import com.taipeigo.product.model.ProductCartFacade;

import jakarta.servlet.http.HttpSession;

@Service
public class CartService {

	@Autowired(required = false)
	private RedisTemplate<String, Object> redisTemplate;
	
	@Autowired
	private ProductCartFacade productCartFacade;

/*===============insertCart======新增購物車====================================================*/
	public void insertCart(CartVO cartVO, HttpSession session) {
		CustomerVO customer = (CustomerVO) session.getAttribute("loginCustomer");
		/**** 會員有登入 ===> 確認Redis有沒有東西 **/
		if (customer != null) {
			Integer custId = customer.getCustId();
			cartVO.setCustId(custId);
			/** Redis沒東西 */
			if (redisTemplate == null) {
				System.out.println("Redis 未啟用");
				return;
			}

			/** redis裡面有被新增 */
			String key = "cart:" + custId;
			/* 去Redis透過Key把Value(opsForValue)拿出放在obj */
			Object obj = redisTemplate.opsForValue().get(key);
			List<CartVO> cartList;
			if (obj == null) {
				/* 如果是空的，就給一個ArrayList讓你裝東西~ */
				cartList = new ArrayList<>();
			} else {
				/** CartVO有東西，拿出並轉型成obj */
				cartList = (List<CartVO>) obj;

			}
			/*** 先預設沒有找到""一樣""的商品，就進入for迴圈，最後再透過把found = true 示意已經完成更改後break出去 **/
			boolean found = false;
			for (CartVO item : cartList) {
				/** 如果VO的productId跟Request進來的CartVO的productId一樣"且"票券日期(ExpiryDate)一樣 "且"商品類型一樣(ACTIVITY,TICKET) "且"票種一樣*/
				if (Objects.equals(item.getProductId(), cartVO.getProductId())
						&& Objects.equals(item.getExpiryDate(), cartVO.getExpiryDate())
						&& Objects.equals(item.getProductType(), cartVO.getProductType())
						&& Objects.equals(item.getSpec(), cartVO.getSpec()))
			
				{
					/** 就直接數量(ProductQuantity)往上加 */
					item.setProductQuantity(item.getProductQuantity() + cartVO.getProductQuantity());
					found = true;
					break;
				}
			}
			/** 購物車本身空的，或是上面判斷都沒有一樣的東西，才新增 **/
			if (!found) {
				cartList.add(cartVO);
			}
			redisTemplate.opsForValue().set(key, cartList);
			System.out.println("加入Redis購物車成功：" + key);

			/** 已登入會員處理完畢，不再往下執行 tempCart */
			return;

		}
		/*** 會員未登入 session站存車叫做=> tempCart ***/
		Object obj = session.getAttribute("tempCart");
		List<CartVO> cartList;
		/**
		 * 暫存車沒有東西，就給你一個購物籃(ArrayList<>) obj有東西就存入，記得轉型
		 */
		if (obj == null) {
			cartList = new ArrayList<>();
		} else {
			cartList = (List<CartVO>) obj;
		}

		boolean found = false;
		for (CartVO item : cartList) {
			if (Objects.equals(item.getProductId(), cartVO.getProductId())
					&& Objects.equals(item.getExpiryDate(), cartVO.getExpiryDate())
					&& Objects.equals(item.getProductType(), cartVO.getProductType())
					&& Objects.equals(item.getSpec(), cartVO.getSpec())) 
			{
				item.setProductQuantity(item.getProductQuantity() + cartVO.getProductQuantity());
				found = true;
				break;
			}
		}

		if (!found) {
			cartList.add(cartVO);
		}
		session.setAttribute("tempCart", cartList);
		System.out.println("加入Session購物車(暫存車tempCart)成功");
	}

/*===============mergeTempCart======整合session跟redis購物車====================================================*/
	/** 登入時合併tempCart進入到Redis購物車 **/
	public void mergeTempCart(HttpSession session) {
		CustomerVO customer = (CustomerVO) session.getAttribute("loginCustomer");
		/** 未登入, tempCart是空的就都return出去 */
		if (customer == null) {
			return;
		}
		Object obj = session.getAttribute("tempCart");
		if (obj == null) {
			return;
		}

		List<CartVO> tempCart = (List<CartVO>) obj;
		for (CartVO cartVO : tempCart) {
			cartVO.setCustId(customer.getCustId());
			insertCart(cartVO, session);
		}
		/** 存好session以後清空 */
		session.removeAttribute("tempCart");
		System.out.println("tempCart已同步到Redis");
	}
	
/*===============queryCart=======搜尋購物車內容===================================================*/
	public List<CartVO> queryCart(HttpSession session) {
		CustomerVO customer = (CustomerVO) session.getAttribute("loginCustomer");

		/** 已登入 ***/
		if (customer != null) {
			Integer custId = customer.getCustId();
			String key = "cart:" + custId;
			/** custId就是Key去找Redis購物車的value=> obj **/
			Object obj = redisTemplate.opsForValue().get(key);
			if (obj == null) {
				return new ArrayList<>();
			}
			return (List<CartVO>) obj;
		}

		/** 未登入 ***/
		Object obj = session.getAttribute("tempCart");
		if (obj == null) {
			return new ArrayList<>();
		}
		return (List<CartVO>) obj;
	}

/*===============updateCart======更新購物車====================================================*/
	public void updateCart(CartVO cartVO, HttpSession session) {
		CustomerVO customer = (CustomerVO) session.getAttribute("loginCustomer");
		/*******************已登入**********************************************/
		if (customer != null) {
			Integer custId = customer.getCustId();
			String key = "cart:" + custId;
			/** custId就是Key去找Redis購物車的value=> obj **/
			Object obj = redisTemplate.opsForValue().get(key);
			if (obj == null) {
				return;
			}
			/**當obj不是空的時，要判斷是否商品是不是一樣，如果是一樣的東西，我就直接更新數字*/
			List<CartVO> cartList = (List<CartVO>) obj;
			for (CartVO item : cartList) {
				if (Objects.equals(item.getProductId(), cartVO.getProductId())
						&& Objects.equals(item.getExpiryDate(), cartVO.getExpiryDate())
						&& Objects.equals(item.getProductType(), cartVO.getProductType())
						&& Objects.equals(item.getSpec(), cartVO.getSpec())) 
				{
					item.setProductQuantity(cartVO.getProductQuantity());
					break;
				}
			}
			/**改好以後再把值存回去Redis中**/
			redisTemplate.opsForValue().set(key, cartList);
			System.out.println("Redis購物車修改成功");
			return;
		}
		/*******************未登入**********************************************/
		Object obj =session.getAttribute("tempCart");
		if (obj == null) {
		    return;
		}
		List<CartVO> cartList =(List<CartVO>) obj;
		for (CartVO item : cartList) {
		    if (Objects.equals(item.getProductId(), cartVO.getProductId())
		    		&& Objects.equals(item.getExpiryDate(), cartVO.getExpiryDate())
		    		&& Objects.equals(item.getProductType(), cartVO.getProductType())
		    		&& Objects.equals(item.getSpec(), cartVO.getSpec())) 
		    {
		        item.setProductQuantity(cartVO.getProductQuantity());
		        break;
		    }
		}
		session.setAttribute("tempCart", cartList);
		System.out.println("Session購物車修改成功");
	}

	
/*===============removeProduct======單一項目刪除====================================================*/
	public void removeProduct(CartVO cartVO, HttpSession session) {
	    CustomerVO customer = (CustomerVO) session.getAttribute("loginCustomer");
		/**已登入**/
		if(customer != null) {
			Integer custId = customer.getCustId();
			String key = "cart:" + custId;
			/** custId就是Key去找Redis購物車的value=> obj **/
			Object obj = redisTemplate.opsForValue().get(key);
			/**如果redis沒存東西**/
			if(obj == null) {
				return;
			}
			List<CartVO> cartList = (List<CartVO>)obj;
			/**整組JSON包成cartList(Redis)，透過for迴圈去把整包分組(i==>第i組)**/
			for(int i =0 ; i < cartList.size(); i++) {
				CartVO item = cartList.get(i);
				if(Objects.equals(item.getProductId(), cartVO.getProductId())
						&& Objects.equals(item.getExpiryDate(), cartVO.getExpiryDate())
						&& Objects.equals(item.getProductType(), cartVO.getProductType())
						&& Objects.equals(item.getSpec(), cartVO.getSpec())) 
				{
					cartList.remove(i);
					break;
				}	
			}
			/**再把更新後的整包set回去redis**/
			redisTemplate.opsForValue().set(key, cartList);
			System.out.println("Redis購物車刪除成功~");
			return;	
		}
		Object obj = session.getAttribute("tempCart");
		if (obj == null) {
		    return;
		}

		List<CartVO> cartList = (List<CartVO>) obj;
		for (int i = 0; i < cartList.size(); i++) {
		    CartVO item = cartList.get(i);
		    if (Objects.equals(item.getProductId(), cartVO.getProductId())
		    		&& Objects.equals(item.getExpiryDate(), cartVO.getExpiryDate())
		    		&& Objects.equals(item.getProductType(), cartVO.getProductType())
		    		&& Objects.equals(item.getSpec(), cartVO.getSpec())) {
		        cartList.remove(i);
		        break;
		    }
		}
		session.setAttribute("tempCart", cartList);
		System.out.println("Session購物車刪除成功");		
	}

	
/*===============clearCart======清空購物車====================================================*/
	public void clearCart(HttpSession session) {
	    CustomerVO customer = (CustomerVO) session.getAttribute("loginCustomer");
		/**已登入**/
	    if(customer != null) {
	    	Integer custId = customer.getCustId();
	    	String key = "cart:" + custId;
	    	redisTemplate.delete(key);
	    	System.out.println("Redis購物車清空成功~");
	    	return;
	    }

	    /**未登入*/
	    session.removeAttribute("tempCart");
	    System.out.println("Session的暫存車tempCart已清空~");

	}
	
/*===============countCart======Header小icon計算購物車商品數====================================================*/
	public Integer countCart(HttpSession session) {
		 List<CartVO> cartList = queryCart(session);
		    Integer total = 0;
		    for (CartVO item : cartList) {
		        total += item.getProductQuantity();
		    }
		    return total;
	}

	
/*===============queryCartItem=======查詢完整購物車資料(顯示給前端畫面)===================================================*/
	public List<CartItemDTO> queryCartItem(HttpSession session) {
		/* 呼叫queryCart(session)方法，
		 * 先從Redis 或 Session 拿到購物車資料 */
		List<CartVO> cartList  = queryCart(session);
		/* 準備一個DTO List裝資料 */
		List<CartItemDTO> cartDTOList = new ArrayList<>();
		for (CartVO vo : cartList ) {
			CartItemDTO dto = productCartFacade.getCartItemInfo(
					vo.getProductType(), 
					vo.getProductId(),
					vo.getProductQuantity(), 
					vo.getSpec());

			if (dto != null) {
				dto.setProductType(vo.getProductType());
				dto.setSpec(vo.getSpec());
				dto.setExpiryDate(vo.getExpiryDate());
				cartDTOList.add(dto);
			}
		}
		return cartDTOList;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

}
