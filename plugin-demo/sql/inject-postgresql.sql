create database inject;
create table if not exists users
(
    id       varchar(32) primary key,
    username varchar(32) not null,
    nickname varchar(32) not null,
    password varchar(32) not null,
    del_flag integer     not null default 0
);
comment on table users is '用户信息表';
comment on column users.id is '账户主键';
comment on column users.username is '账户名';
comment on column users.nickname is '昵称';
comment on column users.password is '密码';
comment on column users.del_flag is '删除标记.0:未删除';
