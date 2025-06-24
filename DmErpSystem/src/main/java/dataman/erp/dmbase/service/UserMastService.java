package dataman.erp.dmbase.service;


import dataman.erp.dmbase.repository.UserMastRepository;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

@Service
public class UserMastService  {

    @Autowired
    private UserMastRepository userMastRepository;





//    @Override
//    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
//
//        System.out.println("Username sent correctly in service layer");
//        UserMast user = userMastRepository.findByUsername(username)
//                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
//
//        return User.builder()
//                .username(user.getUsername())
//                .password(user.getPassword())
//                .roles("USER")
//                .build();
//
//    }

    public boolean isUserExist(String username){
        return userMastRepository.existsByUsername(username);
    }
    public String getUserDescriptionByUserName(String userName){
        return userMastRepository.getDescriptionByUsername(userName);
    }
}
