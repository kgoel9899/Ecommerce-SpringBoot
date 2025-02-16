package com.ecommerce.sb_ecom.service;

import com.ecommerce.sb_ecom.exception.APIException;
import com.ecommerce.sb_ecom.exception.ResourceNotFoundException;
import com.ecommerce.sb_ecom.model.Cart;
import com.ecommerce.sb_ecom.model.CartItem;
import com.ecommerce.sb_ecom.model.Product;
import com.ecommerce.sb_ecom.payload.CartDTO;
import com.ecommerce.sb_ecom.payload.ProductDTO;
import com.ecommerce.sb_ecom.repository.CartItemRepository;
import com.ecommerce.sb_ecom.repository.CartRepository;
import com.ecommerce.sb_ecom.repository.ProductRepository;
import com.ecommerce.sb_ecom.util.AuthUtil;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Stream;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private AuthUtil authUtil;

    @Override
    public CartDTO addProductToCart(Long productId, Integer quantity) {
        Cart cart = createCart();
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId(cart.getId(), productId);

        if (product.getQuantity() == 0) {
            throw new APIException(product.getName() + " is not available");
        }

        if (cartItem != null) {
            throw new APIException("Product " + product.getName() + " already exists in the cart");
        }

        if (product.getQuantity() < quantity) {
            throw new APIException("Please, make an order of the " + product.getName()
                    + " less than or equal to the quantity " + product.getQuantity() + ".");
        }

        CartItem newCartItem = new CartItem();

        newCartItem.setProduct(product);
        newCartItem.setCart(cart);
        newCartItem.setQuantity(quantity);
        newCartItem.setPrice(product.getPrice());

        cartItemRepository.save(newCartItem);

        cart.setTotalPrice(cart.getTotalPrice() + (product.getPrice() * quantity));
        cart.getCartItems().add(newCartItem);

        cartRepository.save(cart);

        CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);

        List<CartItem> cartItems = cart.getCartItems();

        Stream<ProductDTO> productStream = cartItems.stream().map(item -> {
            ProductDTO map = modelMapper.map(item.getProduct(), ProductDTO.class);
            map.setQuantity(item.getQuantity());
            return map;
        });

        cartDTO.setProducts(productStream.toList());

        return cartDTO;
    }

    @Override
    public List<CartDTO> getAllCarts() {
        List<Cart> carts = cartRepository.findAll();

        if (carts.isEmpty()) {
            throw new APIException("No cart exists");
        }

        List<CartDTO> cartDTOs = carts.stream().map(cart -> {
            CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);

            List<ProductDTO> products = cart.getCartItems().stream().map(cartItem -> {
                ProductDTO productDTO = modelMapper.map(cartItem.getProduct(), ProductDTO.class);
                productDTO.setQuantity(cartItem.getQuantity()); // Set the quantity from CartItem
                return productDTO;
            }).toList();

            cartDTO.setProducts(products);

            return cartDTO;

        }).toList();

        return cartDTOs;
    }

    @Override
    public CartDTO getCart(String email, Long cartId) {
        Cart cart = cartRepository.findCartByEmailAndId(email, cartId);
        if(cart == null) {
            throw new ResourceNotFoundException("Cart", "id", cartId);
        }
        CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);
        List<CartItem> cartItems = cart.getCartItems();

        Stream<ProductDTO> productStream = cartItems.stream().map(item -> {
            ProductDTO map = modelMapper.map(item.getProduct(), ProductDTO.class);
            map.setQuantity(item.getQuantity());
            return map;
        });

        cartDTO.setProducts(productStream.toList());

        return cartDTO;
    }

    @Transactional
    @Override
    public CartDTO updateProductQuantityInCart(Long productId, Integer quantity) {

        String emailId = authUtil.loggedInEmail();
        Cart userCart = cartRepository.findCartByEmail(emailId);
        Long cartId = userCart.getId();

        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "id", cartId));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        if (product.getQuantity() == 0) {
            throw new APIException(product.getName() + " is not available");
        }

        if (product.getQuantity() < quantity) {
            throw new APIException("Please, make an order of the " + product.getName()
                    + " less than or equal to the quantity " + product.getQuantity() + ".");
        }

        CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId(cartId, productId);

        if (cartItem == null) {
            throw new APIException("Product " + product.getName() + " not available in the cart!!!");
        }

        int newQuantity = cartItem.getQuantity() + quantity;

        if (newQuantity < 0) {
            throw new APIException("The resulting quantity cannot be negative.");
        }

        if (newQuantity == 0) {
            deleteProductFromCart(cartId, productId);
        } else {
            cartItem.setPrice(product.getPrice());
            cartItem.setQuantity(cartItem.getQuantity() + quantity);
            cartItemRepository.save(cartItem);
            cart.setTotalPrice(cart.getTotalPrice() + (cartItem.getPrice() * quantity));
            cartRepository.save(cart);
        }

        CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);

        List<CartItem> cartItems = cart.getCartItems();

        Stream<ProductDTO> productStream = cartItems.stream().map(item -> {
            ProductDTO prd = modelMapper.map(item.getProduct(), ProductDTO.class);
            prd.setQuantity(item.getQuantity());
            return prd;
        });

        cartDTO.setProducts(productStream.toList());

        return cartDTO;
    }

    // Your custom query likely joins Cart with CartItem and ensures Hibernate recognizes the relationship.
    // findAll() might be returning detached or uninitialized entities, causing cart.getCartItems().remove(cartItem); to not work as expected.

    // 1. By default, in JPA, collections (@OneToMany) are lazily loaded unless explicitly specified otherwise.
    //
    //How It Affects findAll()
    //When you call findAll(), it only retrieves Cart entities and does not load their cartItems unless you explicitly fetch them.
    //When you later call cart.getCartItems().remove(cartItem);, Hibernate might not detect this change, and the removal might not be persisted to the database.
    // so to ensure that cartItems are also fetched, added EAGER fetch type to the OneToMany mapping .... niceeee!


    // 2. findCartsByProductId(productId) Works Because It Likely Joins cartItems
    //Your custom JPQL method findCartsByProductId(productId) probably fetches carts that contain the product along with their cartItems, ensuring Hibernate recognizes the relationships and tracks changes correctly.
    //
    //Why It Works:
    //It might be using an inner join that ensures only relevant carts are retrieved with their cartItems already initialized.

    // i wrote a custom query method 'findByIdd' to check the behaviour -- it got deleted
    // below is why --
    // JPQL (findByIdd(cartId)) works because it always runs a fresh query, returning a fully managed entity.
    // findById(cartId) fails because it may return a detached entity or not fully initialize cartItems.

    // findById(cartId) Returns a Proxy That Might Not Be Fully Managed
    //findById(cartId) loads the entity lazily if it's already in the Persistence Context.
    //If Hibernate detects that the Cart was already loaded in an earlier session, it may return a detached entity,
    // meaning changes to cartItems won’t be tracked.

    // findById() is not working even with EAGER fetchType --
    // it means that cartItems is not properly initialized when using findById(cartId).
    // This strongly suggests that Hibernate is returning a proxy or detached entity, even with EAGER fetch.

    @Transactional
    @Override
    public String deleteProductFromCart(Long cartId, Long productId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "id", cartId));
        CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId(cartId, productId);
        if (cartItem == null) {
            throw new ResourceNotFoundException("Product", "id", productId);
        }
//        String productName = cartItem.getProduct().getName();
        cart.setTotalPrice(cart.getTotalPrice() -
                (cartItem.getPrice() * cartItem.getQuantity()));

        cart.getCartItems().remove(cartItem);
        cartRepository.save(cart);

        // this code alone also works because of the comments above -- tells hibernate about the relationships due to the JOIN FETCH
        // cartItemRepository.deleteCartItemByProductIdAndCartId(cartId, productId);


        // cartItem.getProduct().getName() accesses cartItem.getProduct(), which is a related entity.
        //If Product is lazy-loaded, Hibernate tries to fetch it from the database.
        //But if the transaction is already closing or if cartItem was removed, Hibernate may not allow the lazy loading.
        // hence that is why this return does not delete the cartItem
        // load the product before deleting cartItem --- EVEN THIS IS NOT DELETING THE CARTITEM
//        return "Product " + cartItem.getProduct().getName() + " removed from the cart !!!";

        return "Product removed from the cart !!!";
    }

    @Override
    public void updateProductInCarts(Long cartId, Long productId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "id", cartId));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId(cartId, productId);

        if (cartItem == null) {
            throw new APIException("Product " + product.getName() + " not available in the cart!!!");
        }

        double cartPrice = cart.getTotalPrice() - (cartItem.getPrice() * cartItem.getQuantity());
        cartItem.setPrice(product.getPrice());
        cart.setTotalPrice(cartPrice + (cartItem.getPrice() * cartItem.getQuantity()));

        cartItemRepository.save(cartItem);
    }

    private Cart createCart() {
        Cart userCart = cartRepository.findCartByEmail(authUtil.loggedInEmail());
        if(userCart != null) {
            return userCart;
        }

        Cart cart = new Cart();
        cart.setTotalPrice(0.0);
        cart.setUser(authUtil.loggedInUser());
        return cartRepository.save(cart);
    }
}
