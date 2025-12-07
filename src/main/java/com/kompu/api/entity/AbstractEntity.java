package com.kompu.api.entity;

import java.io.Serializable;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@EqualsAndHashCode
public abstract class AbstractEntity<T extends Serializable> implements Serializable {
    private T id;
}
