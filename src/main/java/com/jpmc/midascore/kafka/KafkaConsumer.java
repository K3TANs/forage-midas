package com.jpmc.midascore.kafka;

import com.jpmc.midascore.entity.TransactionRecord;
import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.foundation.Balance;
import com.jpmc.midascore.foundation.Transaction;
import com.jpmc.midascore.repository.TransactionRepository;
import com.jpmc.midascore.repository.UserRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@Component
public class KafkaConsumer {

    private final UserRepository userRepository;

    private final TransactionRepository transactionRepository;

    private final RestTemplate restTemplate;

    public KafkaConsumer(UserRepository userRepository,TransactionRepository transactionRepository,RestTemplate restTemplate){
        this.userRepository=userRepository;
        this.transactionRepository=transactionRepository;
        this.restTemplate=restTemplate;
    }

    @Transactional
    @KafkaListener(
            id = "midas-group",
            topics = "${general.kafka-topic}"
    )
    public void consume(Transaction transaction){
        UserRecord sender = userRepository.findById(transaction.getSenderId());
        UserRecord recipient = userRepository.findById(transaction.getRecipientId());
        if(sender!=null && recipient!=null && sender.getBalance()> transaction.getAmount()){
            TransactionRecord transactionRecord = new TransactionRecord(transaction.getSenderId(), transaction.getRecipientId(), transaction.getAmount());
            transactionRepository.save(transactionRecord);
            Balance balance=restTemplate.postForObject("http://localhost:8080/incentive",transaction, Balance.class);
            if(balance==null){
                throw new IllegalStateException("Incentive service returned null response");
            }
            float incentiveAmount=balance.getAmount();
            sender.setBalance(sender.getBalance()-transaction.getAmount());
            recipient.setBalance(recipient.getBalance()+transaction.getAmount()+incentiveAmount);
            userRepository.save(sender);
            userRepository.save(recipient);
            System.out.println("----------------------------------------------------------------------------");
            System.out.println(transaction.toString());
            System.out.println("Incentive: "+incentiveAmount);
            System.out.println( sender.getName()+": "+ sender.getBalance() + " and " + recipient.getName() + ": " + recipient.getBalance());
            System.out.println("----------------------------------------------------------------------------");
        }
    }
}
