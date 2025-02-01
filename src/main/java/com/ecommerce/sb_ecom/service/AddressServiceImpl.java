package com.ecommerce.sb_ecom.service;

import com.ecommerce.sb_ecom.exception.ResourceNotFoundException;
import com.ecommerce.sb_ecom.model.Address;
import com.ecommerce.sb_ecom.model.User;
import com.ecommerce.sb_ecom.payload.AddressDTO;
import com.ecommerce.sb_ecom.repository.AddressRepository;
import com.ecommerce.sb_ecom.repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AddressServiceImpl implements AddressService {

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private AddressRepository addressRepository;

    @Override
    public AddressDTO createAddress(AddressDTO addressDTO, User user) {
        Address address = modelMapper.map(addressDTO, Address.class);
        user.getAddresses().add(address);
        address.setUser(user); // this automatically updated the user also, no need to save it explicitly
        return modelMapper.map(addressRepository.save(address), AddressDTO.class);
    }

    @Override
    public List<AddressDTO> getAddresses() {
        List<Address> addresses = addressRepository.findAll();
        return addresses.stream()
                .map(address -> modelMapper.map(address, AddressDTO.class))
                .toList();
    }

    @Override
    public AddressDTO getAddressById(Long addressId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "id", addressId));
        return modelMapper.map(address, AddressDTO.class);
    }

    @Override
    public List<AddressDTO> getUserAddresses(User user) {
        List<Address> addresses = user.getAddresses();
        return addresses.stream()
                .map(address -> modelMapper.map(address, AddressDTO.class))
                .toList();
    }

    @Override
    public AddressDTO updateAddress(Long addressId, AddressDTO addressDTO) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "id", addressId));
        address.setCity(addressDTO.getCity());
        address.setPincode(addressDTO.getPincode());
        address.setState(addressDTO.getState());
        address.setCountry(addressDTO.getCountry());
        address.setStreet(address.getStreet());
        address.setBuilding(address.getBuilding());
        return modelMapper.map(addressRepository.save(address), AddressDTO.class);
    }

    @Override
    public String deleteAddress(Long addressId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address", "id", addressId));
        User user = address.getUser();
        user.getAddresses().removeIf(address1 -> address1.getId().equals(addressId)); // again no need to save the user here (same reason as above)
        addressRepository.delete(address);
        return "Address deleted successfully with the id: " + addressId;
    }
}
