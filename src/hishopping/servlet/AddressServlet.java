package hishopping.servlet;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import hishopping.entity.Address;
import hishopping.entity.User;
import hishopping.service.AddressService;
import hishopping.util.JsonUtil;
import hishopping.util.ServletUtil;

public class AddressServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private AddressService addressService = new AddressService();

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        User user = ServletUtil.currentUser(request);
        if (user == null) {
            JsonUtil.write(response, ServletUtil.fail("请先登录。"));
            return;
        }
        Map<String, Object> result = ServletUtil.ok();
        result.put("addresses", ServletUtil.addresses(addressService.list(user.getId())));
        JsonUtil.write(response, result);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        User user = ServletUtil.currentUser(request);
        if (user == null) {
            JsonUtil.write(response, ServletUtil.fail("请先登录。"));
            return;
        }
        String action = request.getParameter("action");
        if ("add".equals(action)) {
            Address address = new Address();
            address.setUserId(user.getId());
            address.setReceiverName(value(request, "receiverName", user.getUsername()));
            address.setPhone(value(request, "phone", user.getPhone()));
            address.setProvince(value(request, "province", "江苏省"));
            address.setCity(value(request, "city", "南京市"));
            address.setDistrict(value(request, "district", "雨花台区"));
            address.setDetail(value(request, "detail", "软件大道 88 号"));
            address.setDefaultAddress("1".equals(request.getParameter("defaultAddress")));
            addressService.add(address);
        } else if ("default".equals(action)) {
            addressService.setDefault(user.getId(), ServletUtil.intParam(request, "addressId", 0));
        } else if ("delete".equals(action)) {
            addressService.delete(user.getId(), ServletUtil.intParam(request, "addressId", 0));
        }
        Map<String, Object> result = ServletUtil.ok();
        result.put("addresses", ServletUtil.addresses(addressService.list(user.getId())));
        JsonUtil.write(response, result);
    }

    private String value(HttpServletRequest request, String name, String defaultValue) {
        String value = request.getParameter(name);
        return value == null || value.trim().length() == 0 ? defaultValue : value.trim();
    }
}

