module Accounting.Ui exposing
    ( GridPosition(..)
    , LabelText(..)
    , SelectItem
    , SelectItems
    , gridItem
    , makeSelect
    , numberInput
    , textInput
    )

import Html as H exposing (Html)
import Html.Attributes as A
import Html.Events as E
import Json.Decode as JD
import VirtualDom as VD


onChange : (String -> a) -> H.Attribute a
onChange tagger =
    E.on "change" (JD.map tagger E.targetValue)


type InputType
    = InputType String


type LabelText
    = LabelText String


type GridPosition
    = GridPosition String


gridItem : GridPosition -> Html msg -> Html msg
gridItem (GridPosition clazz) item =
    H.div [ A.class clazz ] [ item ]



{-
   <div class="form-group">
     <label for="exampleInputEmail1">Email address</label>
     <input type="email" class="form-control" id="exampleInputEmail1" aria-describedby="emailHelp" placeholder="Enter email">
     <small id="emailHelp" class="form-text text-muted">We'll never share your email with anyone else.</small>
   </div>
-}


input : (String -> msg) -> InputType -> LabelText -> Maybe String -> Html msg
input event (InputType inputType) (LabelText labelText) inputValue =
    let
        myLabel =
            H.label [] [ H.text labelText ]

        myInput =
            case inputValue of
                Nothing ->
                    H.input [ A.type_ inputType, A.class "form-control", E.onInput event ] []

                Just val ->
                    H.input [ A.type_ inputType, A.class "form-control", E.onInput event, A.value val ] []
    in
    H.div [ A.class "form-group" ]
        [ myLabel
        , myInput
        ]


textInput : (String -> msg) -> LabelText -> Maybe String -> Html msg
textInput event labelText inputValue =
    input event (InputType "text") labelText inputValue


numberInput : (String -> msg) -> LabelText -> Maybe String -> Html msg
numberInput event labelText inputValue =
    input event (InputType "number") labelText inputValue


type alias SelectItem =
    { val : String
    , txt : String
    }


type alias SelectItems =
    List SelectItem


emptySelectOption : VD.Node a
emptySelectOption =
    H.option
        [ A.value ""
        ]
        [ H.text "-" ]


makeSelectOption : Maybe String -> SelectItem -> VD.Node a
makeSelectOption selected item =
    case selected of
        Nothing ->
            H.option
                [ A.value item.val
                ]
                [ H.text item.txt ]

        Just sel ->
            H.option
                [ A.value item.val
                , A.selected (sel == item.val)
                ]
                [ H.text item.txt ]


makeSelect : (String -> a) -> String -> SelectItems -> Maybe String -> VD.Node a
makeSelect event caption payload selected =
    let
        makeSelectOption_ =
            makeSelectOption selected

        px =
            emptySelectOption :: List.map makeSelectOption_ payload
    in
    H.span [ A.class "form-group" ]
        [ H.label [] [ H.text caption ]
        , H.select
            [ onChange event
            , A.class "form-control"
            ]
            px
        ]
