package com.picme.weixin.controller;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.picme.common.Constants;
import com.picme.common.RestResult;
import com.picme.entity.Address;
import com.picme.entity.User;
import com.picme.service.AddressService;

@Controller
@RequestMapping(value="weixin/address")
public class AddressController {
	
	@Autowired
	private AddressService addressService;

	@RequestMapping("/list") 
    public ModelAndView list(HttpServletRequest request,Integer albumId) { 
		ModelAndView mv = new ModelAndView("weixin/address/list_of_user");
		User user = (User) request.getSession().getAttribute(Constants.CURRENT_USER_KEY);
		List<Address> addressList = new ArrayList<Address>();
		if(user != null){
			addressList = addressService.listByUserId(user.getId());
		}
		mv.addObject("addressList",addressList);
		mv.addObject("albumId",albumId);
        return mv; 
    }
	
	@RequestMapping("/add") 
    public ModelAndView add(HttpServletRequest request) { 
		ModelAndView mv = new ModelAndView("weixin/address/add");
        return mv;
    }
	
	@ResponseBody
	@RequestMapping("/save") 
    public RestResult<Address> save(HttpServletRequest request,Address address) { 
		RestResult<Address> rest = new RestResult<Address>();
		try {
			addressService.save(address);
			rest.setData(address);
		} catch (Exception e) {
			rest.setError_stack_trace(e.getMessage());
			rest.markAsfailed();
		}
        return rest; 
    }
}