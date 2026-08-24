module Operations where
import Data.List
import FormatToString
import StringToFormat

--remove os coeficientes que são igual a 0 do polinómio
removeZeros::[(Int, [(Char, Int)])] -> [(Int, [(Char, Int)])]
removeZeros []=[]
removeZeros (x:xs) |fst (x)==0 = removeZeros xs
                   |otherwise = x:removeZeros xs
--ordena os elementos [(Char,Int)] de todos os monómios por ordem alfabética, para que o polinómio fique normalizado
sortlistatup::[(Int, [(Char, Int)])] -> [(Int, [(Char, Int)])]
sortlistatup []=[]
sortlistatup (x:xs)= (fst x, sortOn (fst) (snd x)):sortlistatup xs

--função de ordenação primária que ordena pelo grau do monómio
sortP [] = []
sortP p = reverse'' (sortOn(monoDegree) (sortlistatup(p)))

--está ordenado por grau do monómio, estando o de maior grau 1º e assim

--dá reverse a uma lista com elementos de quaisquer tipos
reverse''::[a]->[a]
reverse'' (x:xs)=foldl (\x y->y:x)  [] (x:xs)


--ver se há elementos que têm a mesma variável e expoente e, portanto, podem ser somados
findBool::(Int, [(Char, Int)])->[(Int, [(Char, Int)])]->Bool
findBool _ []=False
findBool e (x:xs)|snd(e)==snd(x) = True
                 |otherwise = findBool e xs

--ver se há elementos que são neutros e, portanto, podem ser somados
findBoolNeutral::(Int, [(Char, Int)])->[(Int, [(Char, Int)])]->Bool
findBoolNeutral _ []=False
findBoolNeutral e (x:xs)|snd(head(snd(x)))==snd(head(snd(e)))&&snd(head(snd(e)))==0  = True
                        |otherwise = findBoolNeutral e xs


--encontra um determinado elemento que pode ser somado com o elemento e
findElem::(Int, [(Char, Int)])->[(Int, [(Char, Int)])]->(Int, [(Char, Int)])
findElem e (x:xs) |snd(x)==snd(e)=(fst x, snd x)
                  |otherwise = findElem e xs

--encontra um determinado elemento neutro que pode ser somado com e
findElemNeutral::(Int, [(Char, Int)])->[(Int, [(Char, Int)])]->(Int, [(Char, Int)])
findElemNeutral e (x:xs) |snd(head(snd(e)))==snd(head(snd(x)))&&snd(head(snd(e)))==0 = (fst(x), snd(x))
                         |otherwise = findElemNeutral e xs


--vê se 2 elementos do polinómio são exatamente iguais
areTheySame :: (Int, [(Char, Int)]) -> (Int, [(Char, Int)])->Bool
areTheySame x y | x == y = True
                | otherwise = False

--vê se 2 elementos neutros do polinómio são exatamente iguais
areTheySameNeutral :: (Int, [(Char, Int)]) -> (Int, [(Char, Int)])->Bool
areTheySameNeutral x y | snd(head(snd(x)))==snd(head(snd(y)))&&snd(head(snd(y)))==0 = True
                       | otherwise = False

--remove um elemento do polinómio
removeItem :: (Int, [(Char, Int)]) -> [(Int, [(Char, Int)])] -> [(Int, [(Char, Int)])]
removeItem i []=[]
removeItem i (x:xs) |areTheySame i x==False = x:removeItem i xs
                    |areTheySameNeutral i x==True = removeItem i xs
                    |otherwise = removeItem i xs


--somar 2 elementos de um polinómio
addElements::(Int, [(Char, Int)])->(Int, [(Char, Int)])->(Int, [(Char, Int)])
addElements a b = (fst a + fst b, snd a)


--simplifica o polinómio, somando todos os elementos que podem ser somados
simplificarPolinomio::[(Int, [(Char, Int)])]->[(Int, [(Char, Int)])]
simplificarPolinomio []=[]
simplificarPolinomio (x:xs) |findBool x xs == True = simplificarPolinomio ((addElements x (findElem x xs)):removeItem (findElem x xs) xs)
                            |findBoolNeutral x xs==True=simplificarPolinomio ((addElements x (findElemNeutral x xs)):removeItem (findElemNeutral x xs) xs)
                            |otherwise = x:simplificarPolinomio xs


--normaliza o polinómio, removendo elemwntos neutros, somando os elementos que podem ser somados e ordenando os elemetntos por maior número de expoente e depois por letra
normalize :: [(Int, [(Char, Int)])] -> [(Int, [(Char, Int)])]
normalize a = removeZeros (simplificarPolinomio (sortP  a))

--apresenta o reusltado da normalização como string
normalizeString::[(Int, [(Char, Int)])]->String
normalizeString (x:xs) = FormatToString.frToString (normalize (x:xs))

strNormalizeString::String->String
strNormalizeString s= normalizeString (strToFr s)

--dá o grau do polinómio
polynomialDegree::[(Int, [(Char, Int)])]->Int
polynomialDegree (x:xs)=somaGraus (snd (head (normalize (x:xs))))

--dá o grau de um monómio
monoDegree::(Int, [(Char,Int)])->Int
monoDegree (x, (y:ys))= somaGraus (y:ys)

--soma os graus de um elemento (que será o grau do polinómio)
somaGraus::[(Char,Int)]->Int
somaGraus []=0
somaGraus (x:xs)=snd(x)+somaGraus(xs)

--soma 2 polinómios, sendo o polinómio resultado da soma normalizado
somaPols::[(Int, [(Char, Int)])] -> [(Int, [(Char, Int)])]->[(Int, [(Char, Int)])]
somaPols [] b=normalize b
somaPols a []=normalize a
somaPols a b= normalize (a++b)

--soma 2 polinómios, apresentando o resultado como uma string
somaPolsString::[(Int, [(Char, Int)])]->[(Int, [(Char, Int)])]->String
somaPolsString a b=FormatToString.frToString (somaPols a b)

--soma 2 polinómios dados como string
strSomaPolsString::String->String->String
strSomaPolsString s t= somaPolsString (StringToFormat.strToFr s) (StringToFormat.strToFr t)

--encontra a variável a derivar na lista com as variáveis
findV::Char->[(Char,Int)]->Bool
findV _ []=False
findV v (x:xs) |v==fst(x)=True
               |otherwise = findV v xs

--remove o elemento com variável n
removeN :: Char -> [(Char,Int)] -> [(Char,Int)]
removeN _ [] = []
removeN n (x:xs)   | n == fst (x) = removeN n xs
                   | otherwise    = x : removeN n xs

--dá o expoente de um elemento
getExpN :: Char -> [(Char, Int)] -> Int
getExpN e (x:xs) |fst(x)==e = snd(x)
                 |otherwise  = getExpN e xs

--deriva um elemento segundo a variável escolhida
deriveElement::Char->(Int, [(Char, Int)])->(Int, [(Char, Int)])
deriveElement v x |findV v (snd(x))==False = (0, [('x',1)])
                  |findV v (snd(x))==True&&length (snd(x))==1 = (fst(x)*(snd (head (snd(x)))), [(v, (snd (head (snd(x))))-1)])
                  |findV v (snd(x))==True&&length (snd(x))>1 = (fst (x) * getExpN v (snd(x)), (v, (getExpN v (snd x)) - 1) : (removeN v (snd x)))

--deriva um polinómio segundo uma variável, derivando cada monómio recursivamente
derivePolynomial::Char->[(Int, [(Char, Int)])]->[(Int, [(Char, Int)])]
derivePolynomial v []=[]
derivePolynomial v (x:xs)=normalize (deriveElement v x:derivePolynomial v xs)

--deriva um polinómio segundo uma variável (o que não é essa variável é tratado como constante), e dá o resultado em string
derivePolynomialString::Char->[(Int, [(Char, Int)])]->String
derivePolynomialString v (x:xs)=FormatToString.frToString (derivePolynomial v (x:xs))

--deriva um polinómio em string segundo uma variável
strDerivePolynomialString :: Char -> String -> String
strDerivePolynomialString v s = derivePolynomialString v (StringToFormat.strToFr s)

--dá a segunda derivada de um polinómio
secondDerivePolynomial::Char->[(Int, [(Char, Int)])]->[(Int, [(Char, Int)])]
secondDerivePolynomial v []=[]
secondDerivePolynomial v (x:xs)=normalize ((deriveElement v (deriveElement v x)):secondDerivePolynomial v xs)

--dá a segunda derivada de um polinómio em string
secondDerivePolynomialString::Char->[(Int, [(Char, Int)])]->String
secondDerivePolynomialString v (x:xs)=FormatToString.frToString (secondDerivePolynomial v (x:xs))

--dá a segunda derivada de um polinómio, recebendo-o como string
strSecondDerivePolynomialString::Char->String->String
strSecondDerivePolynomialString v s= secondDerivePolynomialString v (StringToFormat.strToFr s)
--multiplica o polinómio por uma constante
multiplicateByConstant::[(Int, [(Char, Int)])]->Int->[(Int, [(Char, Int)])]
multiplicateByConstant [] n=[]
multiplicateByConstant (x:xs) n=((fst(x)*n),snd(x)):multiplicateByConstant xs n

--multiplica o polinómio por uma constante e dá o resultado em string
multiplicateByConstantString::[(Int, [(Char, Int)])]->Int->String
multiplicateByConstantString (x:xs) n=FormatToString.frToString (normalize (multiplicateByConstant (x:xs) n))

strMultiplicateByConstantString::String->Int->String
strMultiplicateByConstantString s n=multiplicateByConstantString (StringToFormat.strToFr s) n

--verifica se existe uma variável a, que possa somar com uma variável b
getV::(Char,Int)->[(Char, Int)]->Bool
getV _ []= False
getV n (x:xs)=if fst n == fst x then True else getV n xs

--adiciona expoentes de 2 variáveis
addExp :: (Char,Int) -> (Char,Int) -> (Char,Int)
addExp x y = (fst y, snd x + snd y)

--multiplica as variáveis todas de um monómio
multV::[(Char, Int)]->[(Char, Int)]
multV [] = []
multV (x:xs) | getV x xs = multV  ((addExp x (head(xs))) : tail (xs))
             | otherwise= x : multV xs

--multiplica monómios entre si
multM::(Int, [(Char, Int)]) -> (Int, [(Char, Int)])->(Int, [(Char, Int)])
multM a b= (fst a * fst b, multV (sort(snd a ++ snd b)))

--multiplica 2 polinómios de forma recursiva
multP::[(Int, [(Char, Int)])]->[(Int, [(Char, Int)])]->[(Int, [(Char, Int)])]
multP [] _ = []
multP (x:xs) ys= [multM x y| y <- ys] ++ multP xs ys

--multiplica 2 polinómios normalizados e dá o resultado normalizado
multPN::[(Int, [(Char, Int)])]->[(Int, [(Char, Int)])]->[(Int, [(Char, Int)])]
multPN a b = normalize (multP (normalize a) (normalize b))

--multiplica 2 polinómios, dando o resultado como string
multPNString::[(Int, [(Char, Int)])]->[(Int, [(Char, Int)])]->String
multPNString a b = FormatToString.frToString (multPN a b)

--multiplica 2 polinómios recebidos como string, dando o resultado em string também
strMultPNString::String->String->String
strMultPNString a b = multPNString (StringToFormat.strToFr a) (StringToFormat.strToFr b)
