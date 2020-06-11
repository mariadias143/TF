create table if not exists
    produto(id int identity, nome varchar(45), preco float, stock int);

create table if not exists
    Encomenda(id int identity, idUser varchar(45) , timeout float);

create table if not exists
    Encomenda_Produtos(idEnc int ,idProd int ,quantidade int,PRIMARY KEY(idEnc,idProd));

create table if not exists
    StateUpdate(timestam int identity , type int , userId varchar(45),idEnc int , idProdAdd int , qntProdAdd int );

create table if not exists
    State_RemProd(timestam int , idProd int, quantidade int , PRIMARY KEY(timestam,idProd))
