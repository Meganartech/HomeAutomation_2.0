import LoggedOutNavbar from "./LoggedOutNavbar";

export default function Home() {
    return (

        <>
            <LoggedOutNavbar />

            <div className="container text-center mt-20">
                <h1 className="mb-5">Welcome To Smart Home Automation System</h1>
                <p className="lead">Control and monitor your devices effortlessly with our advanced automation system.</p>
            </div>
        </>
    );
}