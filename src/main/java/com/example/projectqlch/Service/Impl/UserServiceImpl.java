package com.example.projectqlch.Service.Impl;

import com.example.projectqlch.Convert.UserConvert;
import com.example.projectqlch.Entity.SignUpToken;
import com.example.projectqlch.Entity.User;
import com.example.projectqlch.Repository.TokenRepository;
import com.example.projectqlch.Repository.UserRepository;
import com.example.projectqlch.Service.UserService;
import com.example.projectqlch.dto.ChangePasswordRequest;
import com.example.projectqlch.dto.UserDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Value("${file.upload-dir}")
    private String uploadDir;
    @Autowired
    private UserConvert userConvert;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TokenRepository tokenRepository;
    @Autowired
    private EmailService emailService;


    private static final Path CURRENT_FOLDER = Paths.get(System.getProperty("user.dir")+"/src/main/resources/static/UserImage/307691984_811271283242512_124413355601862320_n.jpg");

    @Override
    public User signUp(UserDTO userDTO) {
        User user = userConvert.toEntityUser(userDTO);
        File file = new File(String.valueOf(CURRENT_FOLDER));
        user.setImageUrl(CURRENT_FOLDER.toString());
        user.setActive(false);
        User savedUser = userRepository.save(user);
        generateToken(user);
        return savedUser;
    }

    @Override
    public String verifyToken(String token) {
        Optional<SignUpToken> optionalSignUpToken = tokenRepository.findSignUpTokenByToken(token);
        if (optionalSignUpToken.isEmpty()) {
            return "Activate failed";
        }
        SignUpToken signUpToken = optionalSignUpToken.get();
        User user = signUpToken.getUser();
        user.setActive(true);
        userRepository.save(user);
        return "Activate successfully";
    }

    @Override
    public String deleteUser(Long[] ids) {
        return null;
    }

    @Override
    public UserDTO updateUser(UserDTO userDTO,Long id) {
        User user = userRepository.findUserByIdAndActive(id,true);
        if(user==null) {
            return null;
        }
        user.setName(userDTO.getFullName());
        user.setEmail(userDTO.getEmail());
        user.setAddress(userDTO.getAddress());
        user.setPhone(userDTO.getPhone());
        User savedUser=userRepository.save(user);
        return userConvert.toUserDTO(savedUser);

    }

    @Override
    public UserDTO updateAvatarUser(Long userId, MultipartFile avatar) {
        User user = userRepository.findUserByIdAndActive(userId,true);
        String fileName = StringUtils.cleanPath(Objects.requireNonNull(avatar.getOriginalFilename()));
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            try {
                Files.createDirectories(uploadPath);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        Path file = uploadPath.resolve(fileName);
        try (OutputStream os = Files.newOutputStream(file)) {
            os.write(avatar.getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        user.setImageUrl(String.valueOf(file));
        User savedUser=userRepository.save(user);
        return userConvert.toUserDTO(savedUser);
    }

    @Override
    public UserDTO ChangePassWord(Long userId, ChangePasswordRequest changePasswordRequest) {
        User userSaved = userRepository.findUserByIdAndActive(userId,true);
        if(changePasswordRequest.getPassword().equals(userSaved.getPassword()) ){
            if(changePasswordRequest.getNewPassword().equals(changePasswordRequest.getConfirmNewPassword())){
                userSaved.setPassword(changePasswordRequest.getNewPassword());
                userRepository.save(userSaved);
            }
        }
        return userConvert.toUserDTO(userRepository.save(userSaved));
    }

    public void generateToken(User user){
        UUID token = UUID.randomUUID();
        SignUpToken signUpToken = new SignUpToken();
        signUpToken.setUser(user);
        signUpToken.setToken(String.valueOf(token));
        tokenRepository.save(signUpToken);
        try {
            emailService.sendEmail(user.getEmail(), "Please active your account", "localhost:8088/user/verify/" + token);
        } catch (Exception e) {
            logger.error("Send email failed");
            throw e;
        }
        logger.info("Send email activate successfully");
    }
}
