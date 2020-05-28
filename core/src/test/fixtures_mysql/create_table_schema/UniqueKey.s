CREATE TABLE animals (
    id BIGINT AUTO_INCREMENT,
    name VARCHAR(30) NOT NULL,
    UNIQUE KEY unq_name (name)
);

CREATE TABLE animals (
    id BIGINT AUTO_INCREMENT,
    name VARCHAR(30) NOT NULL,
    UNIQUE INDEX unq_name (name)
);
