package hishopping.service;

import java.util.List;

import hishopping.dao.AddressDao;
import hishopping.entity.Address;

public class AddressService {
    private AddressDao addressDao = new AddressDao();

    public List<Address> list(int userId) {
        return addressDao.findByUserId(userId);
    }

    public List<Address> all() {
        return addressDao.findAll();
    }

    public void add(Address address) {
        addressDao.save(address);
    }

    public void setDefault(int userId, int addressId) {
        addressDao.setDefault(userId, addressId);
    }

    public void delete(int userId, int addressId) {
        addressDao.delete(userId, addressId);
    }
}

