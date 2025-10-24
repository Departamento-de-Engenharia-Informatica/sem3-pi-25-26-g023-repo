CREATE TABLE Linha
(
    idLinha number(10)
GENERATED AS IDENTITY, nome number(10),
                    dono number(10), OperadoridOperador number(10) NOT NULL, PRIMARY KEY (idLinha));
CREATE TABLE SegmentoLinha (idSegmento number(10) GENERATED AS IDENTITY, idLinha number(10), idEstacaoInicio number(10), idEstacaoFim number(10), comprimento number(10), tipo number(10), eletrificado number(10), "Column" number(10), bitola number(10), pesoMaximo number(10), velocidadeMaxima number(10), LinhaidLinha number(10) NOT NULL, EstacaoidEstacao number(10) NOT NULL, PRIMARY KEY (idSegmento));
CREATE TABLE Estacao (idEstacao number(10) GENERATED AS IDENTITY, nome number(10), "Column" number(10), localização number(10), Column2 number(10), PRIMARY KEY (idEstacao));
CREATE TABLE Operador (idOperador number(10) GENERATED AS IDENTITY, nome number(10), PRIMARY KEY (idOperador));
CREATE TABLE Locomotiva (idLocomotiva number(10) GENERATED AS IDENTITY, idOperador number(10), modelo number(10), potência number(10), anoEntrada number(10), peso number(10), capacidadeCombustivel number(10), tipo number(10), bitola number(10), OperadoridOperador number(10) NOT NULL, PRIMARY KEY (idLocomotiva));
CREATE TABLE Vagao (idVagao number(10) GENERATED AS IDENTITY, idOperador number(10), tipo number(10), cargaMaxima number(10), volume number(10), tara number(10), bitola number(10), "Column" number(10), OperadoridOperador number(10) NOT NULL, PRIMARY KEY (idVagao));
CREATE TABLE Linha_Estacao (LinhaidLinha number(10) NOT NULL, EstacaoidEstacao number(10) NOT NULL, PRIMARY KEY (LinhaidLinha, EstacaoidEstacao));
ALTER TABLE Linha ADD CONSTRAINT FKLinha424070 FOREIGN KEY (OperadoridOperador) REFERENCES Operador (idOperador);
ALTER TABLE Locomotiva ADD CONSTRAINT FKLocomotiva235554 FOREIGN KEY (OperadoridOperador) REFERENCES Operador (idOperador);
ALTER TABLE Vagao ADD CONSTRAINT FKVagao414031 FOREIGN KEY (OperadoridOperador) REFERENCES Operador (idOperador);
ALTER TABLE SegmentoLinha ADD CONSTRAINT FKSegmentoLi991280 FOREIGN KEY (LinhaidLinha) REFERENCES Linha (idLinha);
ALTER TABLE SegmentoLinha ADD CONSTRAINT FKSegmentoLi225779 FOREIGN KEY (EstacaoidEstacao) REFERENCES Estacao (idEstacao);
ALTER TABLE Linha_Estacao ADD CONSTRAINT FKLinha_Esta257226 FOREIGN KEY (LinhaidLinha) REFERENCES Linha (idLinha);
ALTER TABLE Linha_Estacao ADD CONSTRAINT FKLinha_Esta959833 FOREIGN KEY (EstacaoidEstacao) REFERENCES Estacao (idEstacao);
