module StringToFormat where
import Data.Char
--import Operations

--transforma um polinómio dado em string num polinómio organizado numa lista de tuplos (p.e, "2*y^2*x^3 + 5*x^2*z^4 + 3*x + 5" = [(2,[('y',2),('x',3)]),(5,[('x',2),('z',4)]),(3,[('x',1)]),(5,[('x',0)])])
strToFr::String->[(Int, [(Char, Int)])]
strToFr s =map (tranftudo) (formanormal(tiraPotencia (trata1efet(tiraVezes (tiraMais (tiraEspaco s))))))

--
lidamenos::String->String
lidamenos []=[]
lidamenos (x:xs) |x=='-'&&(precedingElement x (x:xs))/='^'='+':x:lidamenos xs
                 |otherwise = x:lidamenos xs

precedingElement :: Char -> [Char] -> Char
precedingElement _ [] = '/'
precedingElement _ [x] = '/'
precedingElement elt (x:y:rest)  | y == elt = x
                                 | otherwise = precedingElement elt (y:rest)

--retira os espaços existentes na string (p.e "2*y^2*x^3 + 5*x^2*z^4 + 3*x + 5"="2*y^2*x^3+5*x^2*z^4+3*x+5"")
tiraEspaco::String->String
tiraEspaco p = filter (\n->n/=' ') p

--retira os '+' existentes na string (p.e "2*y^2*x^3+5*x^2*z^4+3*x+5"=["2*y^2*x^3","5*x^2*z^4","3*x","5"])
tiraMais::String->[String]
tiraMais s=tratacoef1lista (split '+' s)

--divide uma string qualquer s segundo um símbolo d (separador)
split :: Eq a => a -> [a] -> [[a]]
split d [] = []
split d s = x : split d (drop 1 y) where (x,y) = span (/= d) s

--retira os '*' existentes na lista de strings (p.e ["2*y^2*x^3","5*x^2*z^4","3*x","5"]=[["2","y^2","x^3"],["5","x^2","z^4"],["3","x"],["5","x^0"]])
tiraVezes :: [String]->[[String]]
tiraVezes []=[]
tiraVezes (x:xs) |allisDigit x==False = [split '*' x] ++ tiraVezes xs
                 |otherwise = [addxElev0 x] ++ tiraVezes xs

--aplica tratacoef1 à lista de strings que temos
tratacoef1lista::[String]->[String]
tratacoef1lista []=[]
tratacoef1lista (x:xs)=tratacoef1 x:tratacoef1lista xs

--trata dos coeficientes que são 1, acrescentado a multiplicação por 1 no início do monómio
tratacoef1::String->String
tratacoef1 (x:xs) |isDigit x==False = "1*"++(x:xs)
                  |otherwise = (x:xs)

--adiciona o expoente a um monómio simples(p.e, x) uma vez que definimos assim na forma dos polinómios
trat1::[String]->[String]
trat1 []=[]
trat1 (x:xs) |allisDigit x==False&&length x==1 = addxElev1 x:trat1 xs
             |otherwise = x:trat1 xs

--aplica a função trat1 a todos os elementos da lista de listas de Strings ([["2","y^2","x^3"],["5","x^2","z^4"],["3","x"],["5","x^0"]]=[["2","y^2","x^3"],["5","x^2","z^4"],["3","x^1"],["5","x^0"]])
trata1efet::[[String]]->[[String]]
trata1efet []=[]
trata1efet (x:xs)=trat1 x:trata1efet xs

--retira os '^' existentes na lista de listas de strings (p.e tiraPotencia [["2","y^2","x^3"],["5","x^2","z^4"],["3","x^1"],["5","x^0"]]=[("2",[["y","2"],["x","3"]]),("5",[["x","2"],["z","4"]]),("3",[["x","1"]]),("5",[["x","0"]])]
tiraPotencia::[[String]]->[(String, [[String]])]
tiraPotencia []=[]
tiraPotencia (x:xs)  = (head x, separa (tail x)) : tiraPotencia xs

--separa elementos de uma lista de strings pelo separador '^'
separa::[String]->[[String]]
separa []=[]
separa (x:xs)= split '^' x:separa xs

--transforma a lista do 2ª elemento dos tuplos em tuplo, aproximando-nos do resultado pretendido no fim
formanormal::[(String, [[String]])]->[(String,[(String, String)])]
formanormal []=[]
formanormal (x:xs)=(fst(x), tupllist (snd(x))):formanormal xs

--tranforma uma lista de listas de strings numa lista de tuplos de strings
tupllist::[[String]]->[(String,String)]
tupllist []=[]
tupllist (x:xs)=tuplify x:tupllist xs
{-
--remove os elementos do polinómio que são nulos, cujo coeficiente é 0 (p.e ["2*y^2","5*x^2","3*x","0*x^3","5*x^3"]=["2*y^2","5*x^2","3*x","5*x^3"])
removeZero::[String]->[String]
removeZero []=[]
removeZero (x:xs)|x!!0=='0'=removeZero xs
                 |otherwise = x:removeZero xs
-}
--verifica se todos os chars da string sao dígitos (números)
allisDigit::String->Bool
allisDigit s=all isDigit s

--transforma um tuplo da forma (x) num tuplo da forma (x,"1") (útil quando temos um termo do polinómio igual a por exemplo a 5*x)
addxElev1::String->String
addxElev1 (x)=x++"^1"

--transforma uma string da forma x numa lista da forma [x,"x^0"] (útil para lidar com as constantes do polinómio)
addxElev0::String->[String]
addxElev0 (x)=[x,"x^0"]

--transforma uma lista com 2 elementos num tuplo com 2 elementos
tuplify::[a]->(a,a)
tuplify [x,y]=(x,y)

--converte os tipos do tuplo abaixo para os tipos do polinómio definidos
tranftudo::(String, [(String, String)])->(Int, [(Char, Int)])
tranftudo (x, (y:ys))=(read x :: Int, tranflist (y:ys))

--converte os tipos da lista de tuplos para os tipos pretendidos
tranflist::[(String, String)]->[(Char, Int)]
tranflist []=[]
tranflist (x:xs)=(head(fst(x)), read (snd(x)) :: Int):tranflist xs
