package com.gzjy.user.controller;


import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.gzjy.common.Add;
import com.gzjy.common.Response;
import com.gzjy.common.annotation.Privileges;
import com.gzjy.common.rest.EpcRestService;
import com.gzjy.role.model.PasswordEdit;
import com.gzjy.user.UserService;
import com.gzjy.user.model.User;


@RestController
@RequestMapping(value = "v1")
public class UserController {
 /* @Autowired
  private OauthUserController oauthUserController;*/
  @Autowired
  private UserService userService;
  @Autowired
  private EpcRestService epcRestService;
  



//添加用户
  @RequestMapping(value = "/organizations/{organizationId}/users", method = RequestMethod.POST)
  @Privileges(name = "USER-ADD", scope = {1})
  public Response add(@PathVariable String organizationId,
      @Validated(value = {Add.class}) @RequestBody User user, BindingResult result) {
    if (result.hasErrors()) {
      return Response.fail(result.getFieldError().getDefaultMessage());
    }
    User u = userService.add(organizationId, user);
    return Response.success(u);
  }
//删除用户
  @Privileges(name = "USER-DELETE", scope = {1})
  @RequestMapping(value = "/organizations/{organizationId}/users/{id}", method = RequestMethod.DELETE)
  public Response delete(@PathVariable String organizationId, @PathVariable("id") String id) {
    int result = userService.delete(organizationId, id);
    return Response.success(result);
  }

  @RequestMapping(value = "/organizations/{organizationId}/users/{id}", method = RequestMethod.PUT)
  @Privileges(name = "USER-EDIT", scope = {1})
  public Response edit(@PathVariable String organizationId, @PathVariable String id,
      @RequestBody User user) {
    User u = userService.update(organizationId, id, user);
    return Response.success(u);
  }

  @RequestMapping(value = "/user", method = RequestMethod.PUT)
  @Privileges(name = "USER-EDITSELF", scope = {1})
  public Response editSelf(@RequestBody User user) {
    User u = userService.updateSelf(user);
    return Response.success(u);
  }

  @RequestMapping(value = "/organizations/{organizationId}/users/{id}/password", method = RequestMethod.PUT)
  @Privileges(name = "USER-PASSWORD", scope = {1})
  public Response editPassword(@PathVariable String organizationId, @PathVariable String id,
      @RequestBody PasswordEdit passwordEdit) {
    User u = userService.updatePassword(organizationId, id, passwordEdit.getOldPassword(),
            passwordEdit.getNewPassword());
    return Response.success(u);
  }

 /* @RequestMapping(value = "/user/validate", method = RequestMethod.POST)
  public Response validatePassword(@RequestBody PasswordEncoder passwordEncoder) {
    User u = userService.validate(passwordEncoder.getPassword());
    return Response.success(u);
  }*/

  @RequestMapping(value = "/user", method = RequestMethod.GET)
  public Response users() {
    return Response.success(userService.getCurrentUser());
  }
  

   @RequestMapping(value = "/user/oauth/token", method = {RequestMethod.GET, RequestMethod.POST})
   public Response token(@RequestParam String username, @RequestParam String password) {
     return userService.getToken(username, password);
   }

  @RequestMapping(value = "/user/privileges", method = RequestMethod.PUT)
  public Response privileges(@RequestBody(required = false) Map<String, Integer> excludes) {
    return Response.success(userService.getPrivilegesOfCurrentUser(excludes));
  }

/*  @RequestMapping(value = "/users/action/logout", method = RequestMethod.GET)
  public Response logout(HttpServletRequest request) {

    boolean logout = oauthUserController.logout(request);
    if (logout) {
      return Response.success(logout);
    } else {
      return Response.success(false);
    }


    // return Response.success(userService.logout());
  }*/
  
  @RequestMapping(value = "/users", method = RequestMethod.GET)
  @Privileges(name = "USER-SELECT", scope = {1})
  public Response users(@RequestParam(required = false) String organizationId,
      @RequestParam(required = false) String roleId, @RequestParam(required = false) Integer status,
      @RequestParam(required = false,defaultValue="1") Integer pageNum,
      @RequestParam(required = false,defaultValue="20") Integer pageSize,
      @RequestParam( name = "name",required = false) String name,
      @RequestParam(name = "order", required = false) String order) {
      if (StringUtils.isBlank(order)) {
          order = "created_at desc";
      }
      Map<String, Object> filter = new HashMap<String, Object>();
      if(!StringUtils.isBlank(name)) {
          filter.put("name", name);
      }
   
    return Response
        .success(userService.getUsers(pageNum, pageSize, order,name));
  }



 



  @RequestMapping(value = "/users/{id}", method = RequestMethod.GET)
  public Response users(@PathVariable String id) {
    return Response.success(userService.getUser(id));
  }

  @RequestMapping(value = "/organizations/{organizationId}/users/{id}", method = RequestMethod.GET)
  public Response users(@PathVariable String organizationId, @PathVariable String id) {
    return Response.success(userService.getUser(organizationId, id));
  }
  
//选取合同评审人员
  @RequestMapping(value = "/users/reviewers", method = RequestMethod.GET)
  public Response reviewers() {
    return Response.success(userService.getReviewers());
  }
  //根据部门查找部门下的用户列表
  @RequestMapping(value = "/organizations/{organizationId}/users", method = RequestMethod.GET)
  public Response getUsersByOrganizationId(@PathVariable("organizationId") String organizationId) {
    return Response.success(userService.selectUsersBasedOrganization(organizationId));
  }
  
  

/*  @RequestMapping(value = "/log/users", method = RequestMethod.GET)
  public Response logusers() {
    return Response.success(userService.getLogUsers());
  }*/


 /* @RequestMapping(value = "/user/kaptcha/request", method = RequestMethod.GET)
  public Response sendKaptcha(HttpServletRequest request, HttpServletResponse response)
      throws Exception {
    return Response.success(userService.getKaptcha(request, response));
  }
  
  @RequestMapping(value = "/user/kaptcha/response", method = RequestMethod.GET)
  public Response receiveKaptcha(@RequestParam String id)
      throws Exception {
    return Response.success(userService.receiveKaptcha(id));
  }*/


  @RequestMapping(value = "/organizations/{organizationId}/users/{userId}/roles/{roleId}/action/assignRole", method = RequestMethod.PUT)
  public Response assign(@PathVariable String organizationId, @PathVariable String userId,
      @PathVariable String roleId) {
    return Response.success(userService.assignRoleForUser(organizationId, userId, roleId));
  }

 

  @RequestMapping(value = "/organizations/{organizationId}/users/{userId}/action/resume", method = RequestMethod.PUT)
  public Response activate(@PathVariable String organizationId, @PathVariable String userId) {
    return Response.success(userService.activate(organizationId, userId));
  }

  @RequestMapping(value = "/organizations/{organizationId}/users/{userId}/action/suspend", method = RequestMethod.PUT)
  public Response suspend(@PathVariable String organizationId, @PathVariable String userId) {
    return Response.success(userService.suspend(organizationId, userId));
  }


  


  
}

