package com.service;

import com.domain.Animal;
import com.repository.AnimalRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.*;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by mac on 2017/12/4.
 */
@Service
public class AnimalService {

    private AnimalRepository animalRepository;

    public AnimalService(AnimalRepository animalRepository) {
        this.animalRepository = animalRepository;
    }
    @Transactional
    public Page<Animal>DynamicFindAnimal(final String name, final Integer age, final String type, final Date  startTime, final Date endTime, final Pageable pageable){
        Page animalPage = animalRepository.findAll(new Specification<Animal>() {
            @Override
            public Predicate toPredicate(Root<Animal> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder cb) {
                Predicate predicate=cb.conjunction();
                List<Expression<Boolean>> expressions=predicate.getExpressions();
                if(name!=null){
                    expressions.add(cb.equal(root.<String>get("name"),name));
                }
                if(age!=null){
                    expressions.add(cb.between(root.<Integer>get("age"),11,15));
                }
                if(type!=null){
                    expressions.add(cb.equal(root.<String>get("type"),type));
                }
                if(startTime!=null){
                    expressions.add(cb.greaterThanOrEqualTo(root.<Date>get("birthday"),startTime));
                }
                if(endTime!=null){
                    expressions.add(cb.lessThanOrEqualTo(root.<Date>get("birthday"),endTime));
                }
                criteriaQuery.where(predicate);
                criteriaQuery.orderBy(cb.desc(root.<Integer>get("age")));
                return criteriaQuery.getRestriction();
            }
        },pageable);
        return animalPage;
    }

    @Cacheable(value = "animal",key = "#animal.id")
    public Animal findAnimal(Animal animal){
        Animal one = animalRepository.findOne(animal.getId());
        return one;
    }
    @CachePut(value = "animal",key = "#result.id")
    public Animal saveAnimal(Animal animal){
        Animal save = animalRepository.save(animal);
        return save;
    }
    /*unless 满足条件的数据不会放到缓存中，但依旧会从缓存中找*/
    @Cacheable(value = "animal",unless ="#result.name.contains('nocache')" )
    public Animal findOneUnless(Long id){
        Animal animal = animalRepository.findOne(id);
        return animal;
    }
    /*condition 不满足条件的不会启动缓存功能，即不会从缓存查，也不会添加到缓存中*/
    @Cacheable(value = "animal",condition = "#id>=5",unless = "#result.name.contentEquals('lisa2')")
    public Animal findOneCondition(Long id){
        Animal animal = animalRepository.findOne(id);
        return animal;
    }
    @Cacheable(value = "animal",key = "#id")
    public Animal findOne(Long id){
        Animal animal = animalRepository.findOne(id);
        return animal;
    }
    @CacheEvict(value = "animal" ,key = "#id")
    public void remove(Long id){
        animalRepository.delete(id);
    }
}

