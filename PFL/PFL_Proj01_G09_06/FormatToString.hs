module FormatToString where
--import Operations

--transforma um elemento (Char,Int)=(Variável, Expoente) num tuplo (String, String), visto que vamos trabalhar com strings neste módulo
tranflista::(Char,Int)->(String, String)
tranflista (x,y)=([x], show y)

--tranforma os elementos do monómio em strings
tranfMon::(Int, [(Char,Int)])->(String, [(String, String)])
tranfMon (x, (y:ys))=(show x, map (tranflista) (y:ys))

--tranforma os elementos todo do polinómio em strings, usando um map
aplicaTudoSub::[(Int, [(Char, Int)])]->[(String, [(String,String)])]
aplicaTudoSub (x:xs)= map (tranfMon)  (x:xs)

--tranforma o 2ª elemento dos monómios numa lista de strings
aplicaTudoI2::[(String, [(String, String)])]->[(String,[String])]
aplicaTudoI2 []=[]
aplicaTudoI2 (x:xs)=(fst x, aplicaTudoI2Mon (snd x)):aplicaTudoI2 xs

--destuplifica o par (Variável, Expoente), tranformando-o numa lista de strings
aplicaTudoI2Mon::[(String, String)]->[String]
aplicaTudoI2Mon []=[]
aplicaTudoI2Mon (x:xs)=detuplify (x)++aplicaTudoI2Mon xs

--"destuplifica" um tuplo, tranformando-o numa lista de 2 elementos
detuplify::(a,a)->[a]
detuplify (x,y)=[x,y]

--liga, no 2º elemento dos monómios, a variável com o expoente, usando um "^"
aplicaTudoI3::[(String, [String])]->[(String,[String])]
aplicaTudoI3 []=[]
aplicaTudoI3 (x:xs) |((snd(x))!!1)=="0"=(fst(x),[" "]):aplicaTudoI3 xs
                    |otherwise = (fst(x), elevate (snd (x))):aplicaTudoI3 xs

--transformação do tuplo de Strings numa lista de listas de Strings, com que é mais fácil de trabalhar
aplicaTudoI4::[(String,[String])]->[[String]]
aplicaTudoI4 []=[]
aplicaTudoI4 (x:xs)= (detuplify(fst(x), toString (snd(x)))):aplicaTudoI4 xs

--trata os monómios que são constantes
trata0::[String]->[String]
trata0 [x,y]  |y==" "=[x]
              |otherwise = [x,y]

--trata da questão dos expoentes=1 no polinómio todo
trata1Pol::[(String,[String])]->[(String,[String])]
trata1Pol []=[]
trata1Pol (x:xs) |snd(x)/=[" "] = (fst(x), trata1 (snd(x))):trata1Pol xs
                 |otherwise = x:trata1Pol xs

--trata os monómios que são simples (expoente=1)
trata1::[String]->[String]
trata1 []=[]
trata1 (x:xs) |x!!1=='^'&&x!!2=='1' = trata1Sub x:trata1 xs
              |otherwise = x: trata1 xs

--remove o "^1"(que é neutro) de uma string
trata1Sub::String->String
trata1Sub s = takeWhile (\n->n/='^'&&n/='1') s

--junta todos os elementos da lista de Strings com um '*', tranformando o polinómio numa string
aplicaTudoI5::[[String]]->[String]
aplicaTudoI5 []=[]
aplicaTudoI5 (x:xs) |x!!0=="1"&&x!!1/=" " = x!!1:aplicaTudoI5 xs
                    |otherwise = addMult x: aplicaTudoI5 xs

--aplica as funções acima todas, transformando o polinómio na sua forma para uma String
frToString::[(Int, [(Char, Int)])]->String
frToString x=addPlus (aplicaTudoI5 (aplicaTudoI4 (trata1Pol (aplicaTudoI3 (aplicaTudoI2 (aplicaTudoSub (x)))))))

--adiciona o operador "^" de exponenciação, juntando assim a variável e o seu expoente
elevate::[String]->[String]
elevate []=[]
elevate (x:y:xs)=[x++"^"++y]++elevate xs

--tranforma a lista de Strings numa String, ligando os elementos com um '*'
toString::[String]->String
toString [x]=x
toString (x:xs)=x++"*"++toString xs

--adiciona o operador '*' de multiplicação, juntando assim a lista de Strings numa só String
addMult::[String]->String
addMult [x," "]=x
addMult [x,y]=x++"*"++y

--liga todas as Strings da lista com o operador '+''
addPlus::[String]->String
addPlus [x]=x
addPlus (x:xs)=x++"+"++addPlus xs
